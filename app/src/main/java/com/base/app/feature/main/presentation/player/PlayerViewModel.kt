package com.base.app.feature.main.presentation.player

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.base.app.core.media.EpisodeFeedPlayer
import com.base.app.core.media.EpisodeFeedPlayerFactory
import com.base.app.core.media.FeedItem
import com.base.app.core.media.PlaybackProgress
import com.base.app.core.media.PlaybackSpeed
import com.base.app.core.media.PlaybackState
import com.base.app.core.media.progress
import com.base.app.core.ui.foundation.error.toUiError
import com.base.app.core.ui.foundation.vm.BaseViewModel
import com.base.app.domain.model.Video
import com.base.app.domain.model.WatchProgress
import com.base.app.domain.usecase.progress.GetResumePositionUseCase
import com.base.app.domain.usecase.progress.SaveWatchProgressUseCase
import com.base.app.domain.usecase.video.GetVideoFeedUseCase
import com.base.app.domain.usecase.video.PickVideoSourceUseCase
import com.base.app.domain.usecase.video.PlaybackQualityPolicy
import com.base.app.feature.main.presentation.player.PlayerScreenProvider.Player
import kotlin.math.abs
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * SAMPLE (video) — ViewModel **sở hữu** [EpisodeFeedPlayer]: xoay máy không dựng lại player, không rebuffer,
 * không mất vị trí. Đổi video (vuốt, tự sang video kế) diễn ra trong VM (không navigate) nên tốc độ/quality giữ
 * nguyên. Video, tốc độ, quality nằm trong [SavedStateHandle] → sống qua process death; vị trí phát lấy lại từ Room.
 *
 * - Resume: mở video nào cũng xem tiếp chỗ đã dừng (Room); lưu mỗi [SAVE_INTERVAL_MS], khi đổi video, pause, vào nền, back.
 * - Vào nền: pause + `setPreloadActive(false)` nhả player lân cận; trở lại thì phát tiếp nếu trước đó đang phát.
 * - Auto quality: buffer quá [PlaybackQualityPolicy.STALL_THRESHOLD_MS] → hạ xuống quality thấp hơn có thật.
 */
class PlayerViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val getVideoFeed: GetVideoFeedUseCase,
    private val pickVideoSource: PickVideoSourceUseCase,
    private val getResumePosition: GetResumePositionUseCase,
    private val saveWatchProgress: SaveWatchProgressUseCase,
    playerFactory: EpisodeFeedPlayerFactory,
) : BaseViewModel<PlayerIntent, PlayerViewState, PlayerSingleEvent>() {

    private val route = savedStateHandle.toRoute<Player>()

    val player: EpisodeFeedPlayer = playerFactory.create()

    /**
     * Tiến độ tách khỏi [viewState]: chỉ thanh track đọc, mỗi tick không recompose cả màn. Gắn với video nó đo
     * ([PlaybackProgress]) để lật sang video mới không vẽ vị trí video trước.
     */
    val playbackProgress: StateFlow<PlaybackProgress> = player.state
        .map { it.progress }
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), PlaybackProgress())

    private var videos: List<Video> = emptyList()

    /** Quality của nguồn đang nạp (Auto thì là quality `pickVideoSource` chọn). */
    private var playingQuality: String? = null

    /** Trần quality do Auto tự hạ khi mạng chậm; null = không hạ. */
    private var autoQualityCap: String? = null
    private var lastSavedPositionMs: Long? = null
    private var wasPlayingBeforeStop = false

    /** Màn đang hiện (giữa ON_START và ON_STOP). Video nạp xong khi đang ở nền thì không được phát tiếng. */
    private var isScreenStarted = true
    private var openJob: Job? = null
    private var stallJob: Job? = null

    private val currentVideo: Video? get() = videos.getOrNull(currentState.current)

    override fun initState() = PlayerViewState(
        speed = savedStateHandle[KEY_SPEED] ?: 1f,
        quality = savedStateHandle[KEY_QUALITY],
    )

    init {
        player.setSpeed(currentState.speed)
        viewModelScope.launch { player.state.collect(::onPlayback) }
        load()
    }

    override fun processIntent(intent: PlayerIntent) {
        when (intent) {
            PlayerIntent.BackClick -> navigateBack()
            PlayerIntent.RetryClick -> retry()
            PlayerIntent.TogglePlay -> togglePlay()
            is PlayerIntent.SeekTo -> seekTo(intent.positionMs)
            PlayerIntent.SpeedClick -> setSpeed(PlaybackSpeed.next(currentState.speed))
            PlayerIntent.OptionsClick -> updateState { copy(optionsVisible = true) }
            PlayerIntent.DismissOptions -> updateState { copy(optionsVisible = false) }
            is PlayerIntent.SpeedSelect -> setSpeed(intent.speed)
            is PlayerIntent.QualitySelect -> selectQuality(intent.quality)
            PlayerIntent.SwitchBackToHigherQuality -> switchBackToHigherQuality()
            PlayerIntent.ToggleDescription -> updateState { copy(descriptionExpanded = !descriptionExpanded) }
            PlayerIntent.ReplayClick -> replay()
            is PlayerIntent.PageSettled -> onPageSettled(intent.page)
            PlayerIntent.ScreenStarted -> onScreenStarted()
            is PlayerIntent.ScreenStopped -> if (!intent.isChangingConfigurations) onScreenStopped()
        }
    }

    private fun load() {
        updateState { copy(isLoading = true, error = null) }
        viewModelScope.launch {
            val loaded = getVideoFeed().getOrElse { error ->
                updateState { copy(isLoading = false, error = error.toUiError()) }
                return@launch
            }
            videos = loaded
            // Process death: vị trí đã lưu trong SavedStateHandle thắng video của route.
            val savedId: String? = savedStateHandle[KEY_VIDEO_ID]
            val start = loaded.indexOfFirst { it.id == (savedId ?: route.videoId) }.coerceAtLeast(0)
            updateState {
                copy(
                    isLoading = false,
                    current = start,
                    pages = loaded.map { PlayerPage(it.id, it.title, it.description, it.thumbnailUrl) },
                )
            }
            refreshPlaylist()
            if (loaded.isNotEmpty()) openVideo(start)
        }
    }

    /**
     * Mở video ở [page] trong player hiện có. [positionMs] null = vị trí resume đã lưu. Mở video mới huỷ lần mở
     * trước còn dở — không thì lần xong sau thắng và player phát nhầm video.
     */
    private fun openVideo(page: Int, positionMs: Long? = null) {
        val video = videos.getOrNull(page) ?: return
        openJob?.cancel()
        stallJob?.cancel()
        savedStateHandle[KEY_VIDEO_ID] = video.id
        lastSavedPositionMs = null
        updateState {
            copy(
                current = page,
                sourceUnavailable = false,
                feedEnded = false,
                descriptionExpanded = false,
                qualities = pickVideoSource.qualitiesOf(video),
            )
        }
        openJob = viewModelScope.launch {
            val start = positionMs ?: getResumePosition(video.id)
            startPlayback(page, video, start, playWhenReady = isScreenStarted)
            // Vào nền lúc còn đang nạp: quay lại thì phát như user mong đợi.
            if (!isScreenStarted) wasPlayingBeforeStop = true
        }
    }

    /** URL của mọi video theo quality đã chọn (hoặc trần Auto) — player lân cận preload đúng bản sẽ phát. */
    private fun refreshPlaylist() {
        val preferred = currentState.quality ?: autoQualityCap
        player.setPlaylist(videos.map { FeedItem(id = it.id, url = pickVideoSource(it, preferred)?.url) })
    }

    private fun startPlayback(page: Int, video: Video, positionMs: Long, playWhenReady: Boolean) {
        val source = pickVideoSource(video, currentState.quality ?: autoQualityCap) ?: run {
            // Một video hỏng không được khoá cả Player: dừng trang này, giữ pager.
            player.setCurrent(page, startPositionMs = 0, playWhenReady = false)
            updateState { copy(sourceUnavailable = true) }
            return
        }
        playingQuality = source.quality
        player.setCurrent(page, positionMs, playWhenReady)
    }

    /** Quality đổi: dựng lại URL cả playlist rồi nạp video hiện tại tại vị trí đang phát. */
    private fun reloadAtCurrentPosition(playWhenReady: Boolean = player.state.value.playWhenReady) {
        // Đọc vị trí trước: playlist mới đổi URL video đang phát → player giải phóng bản cũ, vị trí về 0.
        val positionMs = player.currentPositionMs()
        refreshPlaylist()
        val video = currentVideo ?: return
        startPlayback(currentState.current, video, positionMs, playWhenReady && isScreenStarted)
    }

    private fun onPlayback(playback: PlaybackState) {
        val wasEnded = currentState.playback.ended
        updateState { copy(playback = playback.copy(positionMs = 0, bufferedMs = 0)) }
        if (playback.isPlaying && playback.durationMs > 0) {
            val last = lastSavedPositionMs
            if (last == null || abs(playback.positionMs - last) >= SAVE_INTERVAL_MS) {
                saveProgress(playback.positionMs, playback.durationMs)
            }
        }
        if (playback.ended && !wasEnded) onEnded(playback)
        trackStall(playback)
    }

    private fun onEnded(playback: PlaybackState) {
        saveProgress(playback.durationMs, playback.durationMs)
        val next = currentState.current + 1
        if (next in videos.indices) {
            // Chỉ cuộn; video kế mở khi trang dừng (PageSettled) như user tự vuốt. Đổi video trước khi cuộn
            // thì trang cũ (player bị tua về 0) đổi giữa lúc trượt → flash.
            sendEvent(PlayerSingleEvent.ScrollToPage(next))
        } else {
            updateState { copy(feedEnded = true) }
        }
    }

    /** Buffer quá ngưỡng khi đang muốn phát → Auto hạ xuống quality thấp hơn có thật. */
    private fun trackStall(playback: PlaybackState) {
        if (!(playback.isBuffering && playback.playWhenReady)) {
            stallJob?.cancel()
            stallJob = null
            return
        }
        if (stallJob?.isActive == true) return
        stallJob = viewModelScope.launch {
            delay(STALL_CHECK_MS)
            if (PlaybackQualityPolicy.shouldDowngrade(isAuto = currentState.quality == null, bufferingMs = STALL_CHECK_MS)) {
                downgradeQuality()
            }
        }
    }

    private fun downgradeQuality() {
        val current = playingQuality ?: return
        val target = PlaybackQualityPolicy.downgradeTarget(current, currentState.qualities) ?: return
        autoQualityCap = target
        updateState { copy(qualityDropped = target) }
        reloadAtCurrentPosition(playWhenReady = true)
    }

    private fun switchBackToHigherQuality() {
        autoQualityCap = null
        updateState { copy(qualityDropped = null) }
        reloadAtCurrentPosition()
    }

    /** [final] = lần lưu khi rời/ẩn màn: không để `onCleared` huỷ giữa chừng. */
    private fun saveProgress(positionMs: Long, durationMs: Long, final: Boolean = false) {
        val video = currentVideo ?: return
        if (durationMs <= 0 || currentState.sourceUnavailable) return
        lastSavedPositionMs = positionMs
        val record = WatchProgress(
            videoId = video.id,
            positionMs = positionMs,
            durationMs = durationMs,
            updatedAt = System.currentTimeMillis(),
        )
        if (final) {
            viewModelScope.launch(NonCancellable) { saveWatchProgress(record) }
        } else {
            viewModelScope.launch { saveWatchProgress(record) }
        }
    }

    /** Chỉ lưu khi [PlaybackState] đang nói về video hiện tại — vừa lật trang thì số đo còn là của video trước. */
    private fun saveCurrentProgress(final: Boolean = false) {
        val state = player.state.value
        if (state.itemId != currentVideo?.id) return
        saveProgress(player.currentPositionMs(), state.durationMs, final)
    }

    private fun onScreenStopped() {
        isScreenStarted = false
        wasPlayingBeforeStop = player.state.value.playWhenReady || openJob?.isActive == true
        player.pause()
        player.setPreloadActive(false)
        saveCurrentProgress(final = true)
    }

    /** Chỉ phát lại khi trước lúc vào nền đang phát — user đã pause thì giữ pause. */
    private fun onScreenStarted() {
        isScreenStarted = true
        player.setPreloadActive(true)
        if (wasPlayingBeforeStop) player.play()
        wasPlayingBeforeStop = false
    }

    /** User vuốt tới video khác: lưu chỗ video cũ dừng rồi mở video mới tại chỗ đã xem tới (Room). */
    private fun onPageSettled(page: Int) {
        if (page == currentState.current || page !in videos.indices) return
        saveCurrentProgress()
        updateState { copy(optionsVisible = false) }
        openVideo(page)
    }

    private fun togglePlay() {
        if (currentState.sourceUnavailable) return
        if (player.state.value.playWhenReady) {
            player.pause()
            saveCurrentProgress()
        } else {
            player.play()
        }
    }

    private fun seekTo(positionMs: Long) {
        player.seekTo(positionMs)
        updateState { copy(feedEnded = false) }
    }

    private fun setSpeed(speed: Float) {
        savedStateHandle[KEY_SPEED] = speed
        player.setSpeed(speed)
        updateState { copy(speed = speed) }
    }

    private fun selectQuality(quality: String?) {
        savedStateHandle[KEY_QUALITY] = quality
        autoQualityCap = null
        updateState { copy(quality = quality, qualityDropped = null) }
        reloadAtCurrentPosition()
    }

    private fun replay() {
        updateState { copy(feedEnded = false) }
        player.seekTo(0)
        player.play()
    }

    private fun retry() {
        if (videos.isEmpty()) {
            load()
        } else {
            reloadAtCurrentPosition(playWhenReady = true)
        }
    }

    private fun navigateBack() {
        saveCurrentProgress(final = true)
        sendEvent(PlayerSingleEvent.NavigateBack)
    }

    override fun onCleared() {
        player.release()
        super.onCleared()
    }

    companion object {
        const val KEY_VIDEO_ID = "player_video_id"
        const val KEY_SPEED = "player_speed"
        const val KEY_QUALITY = "player_quality"

        private const val SAVE_INTERVAL_MS = 5_000L
        private const val STOP_TIMEOUT_MS = 5_000L
        private const val STALL_CHECK_MS = PlaybackQualityPolicy.STALL_THRESHOLD_MS + 100
    }
}
