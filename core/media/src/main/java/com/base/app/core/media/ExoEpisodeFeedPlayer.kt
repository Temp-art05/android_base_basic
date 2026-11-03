package com.base.app.core.media

import android.content.Context
import android.os.SystemClock
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.analytics.AnalyticsListener
import androidx.media3.exoplayer.mediacodec.MediaCodecSelector
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.exoplayer.upstream.DefaultBandwidthMeter
import kotlin.math.abs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * [EpisodeFeedPlayer] trên một **pool** ExoPlayer (`MediaProfile.poolSize`: 3 máy thường, 2 máy yếu)
 * — kiểu TikTok. Tập hiện tại phát; mỗi tập lân cận có player riêng đã `prepare` và đứng **pause** ở
 * giây 0, nên trang lân cận đã có khung hình thật và vài giây đầu trong buffer: vuốt tới chỉ cần `play()`.
 * Tập vừa rời được tua về 0 (sau khi trang trượt khuất), nên vuốt ngược cũng tức thì.
 *
 * - Player giữ một tập còn trong cửa sổ thì không bị động tới khi vuốt ([assignSlots]); player rời
 *   cửa sổ được tái dùng cho tập mới.
 * - Player lân cận câm, không giữ audio focus, tải tối đa vài giây ([RoleLoadControl]); chỉ prepare +
 *   tải sau khi tập hiện tại lên hình và trang đứng yên một lúc (lướt nhanh không tốn decoder).
 * - Mọi player đọc qua cùng cache đĩa (`CacheDataSource`) và chung bandwidth meter.
 * - ExoPlayer của một slot chỉ được dựng khi slot nhận tập đầu tiên: feed một mục (preview Browse)
 *   không tốn thread/RAM cho hai player không bao giờ dùng.
 * - [setActive] `false` nhả hết decoder của pool khi feed bị che (tab ẩn, mở Player).
 *
 * Tạo và gọi trên main thread.
 */
@OptIn(UnstableApi::class)
class ExoEpisodeFeedPlayer(
    context: Context,
    private val profile: MediaProfile,
    dataSourceFactory: DataSource.Factory,
) : EpisodeFeedPlayer {

    private val appContext = context.applicationContext
    private val mediaSourceFactory = DefaultMediaSourceFactory(dataSourceFactory)
    private val renderersFactory = DefaultRenderersFactory(appContext)
        // Decoder đầu tiên khởi tạo lỗi (vd. hết decoder phần cứng) thì thử decoder kế thay vì dừng phát.
        .setEnableDecoderFallback(true)
        .setMediaCodecSelector(EmulatorLastCodecSelector)
    private val bandwidthMeter = DefaultBandwidthMeter.getSingletonInstance(appContext)
    private val audioAttributes = AudioAttributes.Builder()
        .setUsage(C.USAGE_MEDIA)
        .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
        .build()

    private val pool: List<Slot> = List(profile.poolSize) { Slot(it) }

    private var items: List<FeedItem> = emptyList()
    private var current = 0
    private var currentSlot: Slot? = null
    private var preloadActive = true
    private var active = true
    private var muted = false
    private var speed = 1f
    private var firstFrameRequestedAt = 0L

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var ticker: Job? = null
    private var warmUpJob: Job? = null

    private val _state = MutableStateFlow(PlaybackState())
    override val state: StateFlow<PlaybackState> = _state.asStateFlow()

    private val _slots = MutableStateFlow<Map<Int, VideoSlot>>(emptyMap())
    override val slots: StateFlow<Map<Int, VideoSlot>> = _slots.asStateFlow()

    /** Một player của pool. [position]/[url] = tập đang giữ; null = rảnh. */
    internal inner class Slot(val id: Int) : VideoSlot {
        private val loadControl = RoleLoadControl(profile)
        private var built: ExoPlayer? = null

        /** Dựng lười ở lần dùng đầu (thường là [load]). */
        val exoPlayer: ExoPlayer
            get() = built ?: buildPlayer().also { built = it }

        private fun buildPlayer(): ExoPlayer = ExoPlayer.Builder(appContext)
            .setMediaSourceFactory(mediaSourceFactory)
            .setRenderersFactory(renderersFactory)
            .setLoadControl(loadControl)
            .setBandwidthMeter(bandwidthMeter)
            .setTrackSelector(
                DefaultTrackSelector(appContext).apply {
                    profile.maxVideoSize?.let { (width, height) ->
                        setParameters(buildUponParameters().setMaxVideoSize(width, height))
                    }
                },
            )
            .setAudioAttributes(audioAttributes, /* handleAudioFocus = */ false)
            .build()
            .also { player ->
                player.addListener(listener)
                player.addAnalyticsListener(diagnostics)
            }

        var position: Int? = null
        var url: String? = null

        /** Đã `prepare` (decoder + khung đầu). Lân cận chỉ prepare khi trang hiện tại đã đứng yên. */
        private var prepared = false

        private val _firstFrameRendered = MutableStateFlow(false)
        override val firstFrameRendered: StateFlow<Boolean> = _firstFrameRendered.asStateFlow()

        private val listener = object : Player.Listener {
            override fun onEvents(player: Player, events: Player.Events) {
                if (this@Slot !== currentSlot) return
                publish()
                if (player.isPlaying) startTicker() else stopTicker()
            }

            override fun onRenderedFirstFrame() {
                _firstFrameRendered.value = true
                if (this@Slot === currentSlot) {
                    // Chỉ đo lần đầu sau mỗi lần đổi tập; tua / gắn lại surface cũng bắn lại callback này.
                    if (firstFrameRequestedAt > 0) {
                        Timber.d("FirstFrame %d ms (slot %d, from pool=false)", SystemClock.elapsedRealtime() - firstFrameRequestedAt, id)
                        firstFrameRequestedAt = 0
                    }
                    allowNeighborLoading()
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                Timber.w(error, "Playback error %s (slot %d, position %s)", error.errorCodeName, id, position)
                if (this@Slot === currentSlot) {
                    _state.update { it.copy(error = PlaybackError(error.errorCodeName)) }
                } else {
                    // Lân cận lỗi (vd. hết decoder): bỏ, trang đó hiện poster; tập đang phát không sao.
                    free()
                }
            }
        }

        /** Chẩn đoán tiếng rè / giật: underrun = audio track hết dữ liệu (CPU/decoder không theo kịp). */
        private val diagnostics = object : AnalyticsListener {
            override fun onAudioUnderrun(
                eventTime: AnalyticsListener.EventTime,
                bufferSize: Int,
                bufferSizeMs: Long,
                elapsedSinceLastFeedMs: Long,
            ) {
                Timber.w("AudioUnderrun slot %d: buffer %d ms, %d ms since last feed", id, bufferSizeMs, elapsedSinceLastFeedMs)
            }

            override fun onDroppedVideoFrames(eventTime: AnalyticsListener.EventTime, droppedFrames: Int, elapsedMs: Long) {
                Timber.d("DroppedFrames slot %d: %d in %d ms", id, droppedFrames, elapsedMs)
            }
        }

        fun load(position: Int, item: FeedItem, url: String, startPositionMs: Long, prepareNow: Boolean) {
            Timber.d("Slot %d load position %d at %d ms (prepare now=%b)", id, position, startPositionMs, prepareNow)
            this.position = position
            this.url = url
            _firstFrameRendered.value = false
            prepared = false
            exoPlayer.setMediaItem(MediaItem.Builder().setMediaId(item.id).setUri(url).build(), startPositionMs)
            exoPlayer.playWhenReady = false
            if (prepareNow) ensurePrepared()
        }

        fun ensurePrepared() {
            if (position == null) return
            // Lỗi (vd. mất mạng giữa chừng) đưa player về IDLE: retry phải prepare() lại, không thì kẹt màn lỗi.
            if (prepared && built?.playerError == null) return
            prepared = true
            exoPlayer.prepare()
        }

        /** Vai hiện tại: có tiếng (trừ khi [muted]), giữ audio focus, tải như thường. */
        fun becomeCurrent() {
            loadControl.neighborCapMs = null
            exoPlayer.volume = if (muted) 0f else 1f
            exoPlayer.setAudioAttributes(audioAttributes, /* handleAudioFocus = */ !muted)
            exoPlayer.setHandleAudioBecomingNoisy(!muted)
        }

        /** Vai lân cận: pause, câm; chưa tải gì tới khi [warmUp]. */
        fun becomeNeighbor(capMs: Long) {
            exoPlayer.playWhenReady = false
            exoPlayer.volume = 0f
            exoPlayer.setAudioAttributes(audioAttributes, /* handleAudioFocus = */ false)
            exoPlayer.setHandleAudioBecomingNoisy(false)
            loadControl.neighborCapMs = capMs
            loadControl.neighborLoadingAllowed = false
        }

        /** Lân cận bắt đầu: prepare (khung đầu) + tải vài giây đầu. */
        fun warmUp() {
            loadControl.neighborLoadingAllowed = true
            ensurePrepared()
        }

        fun free() {
            Timber.d("Slot %d free position %s", id, position)
            position = null
            url = null
            _firstFrameRendered.value = false
            prepared = false
            // stop() đưa player về IDLE: renderer nhả decoder và buffer RAM.
            built?.stop()
            built?.clearMediaItems()
            publishSlots()
        }

        fun release() {
            built?.removeListener(listener)
            built?.release()
            built = null
        }
    }

    override fun setPlaylist(items: List<FeedItem>) {
        this.items = items
        // URL đổi (quality/ngôn ngữ) → bản đã nạp vô dụng. Tập hiện tại nạp lại ở setCurrent kế tiếp.
        pool.forEach { slot ->
            val position = slot.position ?: return@forEach
            if (items.getOrNull(position)?.url != slot.url) slot.free()
        }
        if (currentSlot?.position == current) reassign()
    }

    override fun setCurrent(position: Int, startPositionMs: Long, playWhenReady: Boolean) {
        val item = items.getOrNull(position) ?: return
        if (!active) {
            // Feed đang bị che: chỉ nhớ vị trí, setActive(true) dựng lại đúng tập này.
            current = position
            return
        }
        val previous = currentSlot
        previous?.exoPlayer?.playWhenReady = false
        warmUpJob?.cancel()
        current = position
        firstFrameRequestedAt = SystemClock.elapsedRealtime()
        stopTicker()
        if (item.url == null) {
            currentSlot = null
            reassign()
            _state.value = PlaybackState()
            return
        }
        reassign(currentStartMs = startPositionMs)
        val slot = pool.first { it.position == position }
        currentSlot = slot
        if (previous != null && previous !== slot && previous.position != null) resetWhenOffscreen(previous)
        // Player lân cận đứng ở vị trí resume cũ; vị trí mới lệch nhiều (vd. đổi quality) thì tua.
        if (abs(slot.exoPlayer.currentPosition - startPositionMs) > SEEK_TOLERANCE_MS) slot.exoPlayer.seekTo(startPositionMs)
        slot.exoPlayer.setPlaybackSpeed(speed)
        slot.becomeCurrent()
        // Lướt nhanh tới trang lân cận chưa kịp warm-up: prepare ngay.
        slot.ensurePrepared()
        slot.exoPlayer.playWhenReady = playWhenReady
        val fromPool = slot.firstFrameRendered.value
        if (fromPool) {
            Timber.d("FirstFrame 0 ms (slot %d, from pool=true)", slot.id)
            firstFrameRequestedAt = 0
            allowNeighborLoading()
        }
        publish()
    }

    override fun setPreloadActive(active: Boolean) {
        if (preloadActive == active) return
        preloadActive = active
        reassign()
    }

    override fun setActive(active: Boolean) {
        if (this.active == active) return
        this.active = active
        if (active) {
            Timber.d("Pool active again at position %d", current)
            setCurrent(current, startPositionMs = 0, playWhenReady = false)
            return
        }
        Timber.d("Pool inactive: freeing every player")
        warmUpJob?.cancel()
        stopTicker()
        currentSlot = null
        pool.filter { it.position != null }.forEach(Slot::free)
        _state.update { it.copy(isPlaying = false, playWhenReady = false, isBuffering = false) }
    }

    override fun setMuted(muted: Boolean) {
        if (this.muted == muted) return
        this.muted = muted
        currentSlot?.becomeCurrent()
    }

    override fun play() {
        val player = currentSlot?.exoPlayer ?: return
        // Hết tập rồi bấm play = xem lại từ đầu, như mọi player.
        if (player.playbackState == Player.STATE_ENDED) player.seekTo(0)
        player.play()
    }

    override fun pause() {
        currentSlot?.exoPlayer?.pause()
    }

    override fun seekTo(positionMs: Long) {
        currentSlot?.exoPlayer?.seekTo(positionMs)
        publish()
    }

    override fun setSpeed(speed: Float) {
        this.speed = speed
        currentSlot?.exoPlayer?.setPlaybackSpeed(speed)
    }

    override fun currentPositionMs(): Long = currentSlot?.exoPlayer?.currentPosition ?: 0

    override fun release() {
        scope.cancel()
        pool.forEach(Slot::release)
        currentSlot = null
        _slots.value = emptyMap()
    }

    /**
     * Đưa pool về đúng cửa sổ quanh [current]: giữ player đang giữ tập vẫn cần, nạp tập mới vào player
     * rảnh, nhả player rời cửa sổ. Ở nền ([preloadActive] false) chỉ giữ tập hiện tại.
     */
    private fun reassign(currentStartMs: Long? = null) {
        if (!active) return
        val playable: (Int) -> Boolean = { items[it].url != null }
        val wanted = if (preloadActive) {
            poolPositions(current, items.size, profile, playable)
        } else {
            listOf(current).filter { it in items.indices && playable(it) }
        }
        val assignment = assignSlots(pool.map { it.position }, wanted)
        // Tập hiện tại khoá thì không có gì để nhường băng thông.
        val currentReady = items.getOrNull(current)?.url == null ||
            pool.firstOrNull { it.position == current }?.firstFrameRendered?.value == true
        pool.forEachIndexed { index, slot ->
            val target = assignment[index]
            if (target == null) {
                if (slot.position != null) slot.free()
                return@forEachIndexed
            }
            if (slot.position != target) {
                val item = items[target]
                val start = if (target == current) currentStartMs ?: 0 else 0
                slot.load(target, item, item.url ?: return@forEachIndexed, start, prepareNow = target == current)
            }
            if (target != current) slot.becomeNeighbor(profile.neighborBufferMs[target - current] ?: 0)
        }
        publishSlots()
        if (currentReady) allowNeighborLoading()
    }

    /**
     * Tập vừa rời (giờ là lân cận) về giây 0 — khung đầu + buffer đầu sẵn cho lần vuốt ngược. Đợi trang
     * trượt khuất rồi mới tua: tua ngay thì trang đang trượt đi nhảy sang cảnh đầu tập (flash), nhất là
     * khi tự sang tập / chọn tập mà pager còn đang cuộn.
     */
    /**
     * Tập vừa rời tua về 0 **ngay**: caller chỉ đổi tập khi trang cũ đã khuất (`PageSettled`), nên vuốt ngược lại
     * thấy đúng khung giây 0 + track 0% — không còn cảnh khung hình cũ (giây 5) rồi nhảy về đầu trước mắt.
     */
    private fun resetWhenOffscreen(slot: Slot) {
        slot.exoPlayer.seekTo(0)
    }

    /**
     * Tập hiện tại đã lên hình → sau [NEIGHBOR_WARMUP_DELAY_MS] player lân cận mới prepare + tải vài giây.
     * Lướt nhanh qua nhiều trang thì mỗi lần đổi tập huỷ lượt warm-up trước: không tốn decoder/CPU cho
     * trang chỉ lướt qua (đo trên emulator: lướt 10 trang liên tục đẩy decoder + app lên ~150% CPU).
     */
    private fun allowNeighborLoading() {
        warmUpJob?.cancel()
        warmUpJob = scope.launch {
            delay(NEIGHBOR_WARMUP_DELAY_MS)
            pool.filter { it !== currentSlot && it.position != null }.forEach(Slot::warmUp)
        }
    }

    private fun publishSlots() {
        _slots.value = pool.mapNotNull { slot -> slot.position?.let { it to slot } }.toMap()
    }

    private fun publish() {
        val player = currentSlot?.exoPlayer ?: return
        val duration = player.duration.takeIf { it != C.TIME_UNSET && it > 0 } ?: 0
        _state.update { state ->
            state.copy(
                itemId = items.getOrNull(current)?.id,
                isPlaying = player.isPlaying,
                playWhenReady = player.playWhenReady,
                isBuffering = player.playbackState == Player.STATE_BUFFERING,
                positionMs = player.currentPosition,
                durationMs = duration,
                bufferedMs = player.bufferedPosition,
                ended = player.playbackState == Player.STATE_ENDED,
                // Chuẩn bị lại (đổi tập / retry) thì xoá lỗi cũ.
                error = if (player.playerError == null) null else state.error,
            )
        }
    }

    /** Vị trí không có callback riêng: chỉ poll khi đang phát, dừng thì thôi để không tốn pin. */
    private fun startTicker() {
        if (ticker?.isActive == true) return
        ticker = scope.launch {
            while (isActive) {
                publish()
                delay(TICK_MS)
            }
        }
    }

    private fun stopTicker() {
        ticker?.cancel()
        ticker = null
    }

    private companion object {
        const val TICK_MS = 500L
        const val SEEK_TOLERANCE_MS = 1_000L

        /** Trang phải đứng yên chừng này mới chuẩn bị trang lân cận. */
        const val NEIGHBOR_WARMUP_DELAY_MS = 300L


        /**
         * Decoder `c2.goldfish.*` của Android Emulator lỗi giữa chừng với H.264 1080×1920 của fixtures
         * (`ERROR_CODE_DECODING_FAILED`) → đẩy nó xuống cuối để dùng decoder phần mềm. Máy thật không
         * có decoder goldfish nên thứ tự giữ nguyên.
         */
        val EmulatorLastCodecSelector = MediaCodecSelector { mimeType, requiresSecure, requiresTunneling ->
            MediaCodecSelector.DEFAULT.getDecoderInfos(mimeType, requiresSecure, requiresTunneling)
                .sortedBy { it.name.startsWith("c2.goldfish") }
        }
    }
}
