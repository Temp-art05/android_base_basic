package com.base.app.feature.main.presentation.main.reels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
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
import com.base.app.domain.usecase.video.GetVideoFeedUseCase
import com.base.app.domain.usecase.video.PickVideoSourceUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * SAMPLE (video) — feed dọc kiểu TikTok, **cùng engine với Player**: [EpisodeFeedPlayer] là pool player
 * (hiện tại + lân cận đã prepare + pause, cache đĩa theo phiên). VM sở hữu player → xoay máy không rebuffer.
 *
 * - Chỉ phát khi tab Reels đang chọn **và** màn STARTED; ngoài ra `setActive(false)` nhả hết decoder — mở Player
 *   từ đây không bao giờ có hai pool cùng giữ decoder (máy yếu).
 * - Vuốt tới reel nào cũng phát từ đầu; không ghi tiến độ (xem thử). Hết reel → tự cuộn sang reel kế.
 */
class ReelsViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val getVideoFeed: GetVideoFeedUseCase,
    private val pickVideoSource: PickVideoSourceUseCase,
    playerFactory: EpisodeFeedPlayerFactory,
) : BaseViewModel<ReelsIntent, ReelsViewState, ReelsSingleEvent>() {

    val player: EpisodeFeedPlayer = playerFactory.create()

    /** Tiến độ tách khỏi [viewState] (tick không recompose cả màn), gắn với reel nó đo — xem [PlaybackProgress]. */
    val playbackProgress: StateFlow<PlaybackProgress> = player.state
        .map { it.progress }
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), PlaybackProgress())

    private var videos: List<Video> = emptyList()

    private var tabVisible = false
    private var started = false
    private var active = false

    /** Lần đầu hiện tab thì tự phát; user pause rồi rời tab thì quay lại vẫn pause. */
    private var playOnActive = true

    override fun initState() = ReelsViewState(
        current = savedStateHandle[KEY_CURRENT] ?: 0,
        speed = savedStateHandle[KEY_SPEED] ?: 1f,
    )

    init {
        player.setActive(false)
        player.setSpeed(currentState.speed)
        viewModelScope.launch { player.state.collect(::onPlayback) }
        load()
    }

    override fun processIntent(intent: ReelsIntent) {
        when (intent) {
            is ReelsIntent.PageSettled -> onPageSettled(intent.page)
            ReelsIntent.TogglePlay -> togglePlay()
            ReelsIntent.CycleSpeed -> cycleSpeed()
            ReelsIntent.ToggleDescription -> updateState { copy(descriptionExpanded = !descriptionExpanded) }
            ReelsIntent.OpenPlayerClick -> currentVideo()?.let { sendEvent(ReelsSingleEvent.OpenPlayer(it.id)) }
            ReelsIntent.Retry -> if (!currentState.isLoading) load()
            ReelsIntent.RetryPlayback -> player.setCurrent(currentState.current, startPositionMs = 0, playWhenReady = active)
            is ReelsIntent.TabVisible -> {
                tabVisible = intent.visible
                applyActive()
            }
            ReelsIntent.ScreenStarted -> {
                started = true
                applyActive()
            }
            is ReelsIntent.ScreenStopped -> if (!intent.isChangingConfigurations) {
                started = false
                applyActive()
            }
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
            val current = currentState.current.coerceIn(0, (loaded.size - 1).coerceAtLeast(0))
            updateState {
                copy(
                    isLoading = false,
                    current = current,
                    pages = loaded.map { ReelPage(it.id, it.title, it.description, it.thumbnailUrl) },
                )
            }
            // Feed không cần quality cao nhất: preload lân cận rẻ hơn. Đổi trần ở [FEED_QUALITY_CAP].
            player.setPlaylist(loaded.map { FeedItem(id = it.id, url = pickVideoSource(it, FEED_QUALITY_CAP)?.url) })
            if (loaded.isNotEmpty()) player.setCurrent(current, startPositionMs = 0, playWhenReady = active && playOnActive)
        }
    }

    private fun onPageSettled(page: Int) {
        if (page == currentState.current || page !in videos.indices) return
        savedStateHandle[KEY_CURRENT] = page
        updateState { copy(current = page, descriptionExpanded = false) }
        // Vuốt sang reel mới = muốn xem: phát (nếu tab đang hiện) dù reel trước đang pause.
        playOnActive = true
        player.setCurrent(page, startPositionMs = 0, playWhenReady = active)
    }

    private fun applyActive() {
        val shouldBeActive = tabVisible && started
        if (shouldBeActive == active) return
        active = shouldBeActive
        if (active) {
            player.setActive(true)
            if (playOnActive && videos.isNotEmpty()) player.play()
        } else {
            // Đang phát → quay lại phát tiếp; feed còn đang tải thì giữ ý định cũ; user đã pause thì giữ pause.
            playOnActive = player.state.value.playWhenReady || (currentState.isLoading && playOnActive)
            player.pause()
            player.setActive(false)
        }
    }

    private fun onPlayback(playback: PlaybackState) {
        val wasEnded = currentState.playback.ended
        updateState { copy(playback = playback.copy(positionMs = 0, bufferedMs = 0)) }
        if (playback.ended && !wasEnded) {
            val next = currentState.current + 1
            if (next in videos.indices) {
                // Chỉ cuộn; reel kế mở ở PageSettled như user tự vuốt.
                sendEvent(ReelsSingleEvent.ScrollToPage(next))
            } else {
                player.seekTo(0)
                player.play()
            }
        }
    }

    private fun togglePlay() {
        if (player.state.value.playWhenReady) {
            player.pause()
            playOnActive = false
        } else {
            player.play()
            playOnActive = true
        }
    }

    private fun cycleSpeed() {
        val speed = PlaybackSpeed.next(currentState.speed)
        savedStateHandle[KEY_SPEED] = speed
        player.setSpeed(speed)
        updateState { copy(speed = speed) }
    }

    private fun currentVideo(): Video? = videos.getOrNull(currentState.current)

    override fun onCleared() {
        player.release()
        super.onCleared()
    }

    companion object {
        const val KEY_CURRENT = "reels_current"
        const val KEY_SPEED = "reels_speed"

        /** Trần quality của feed. Video không có đúng bản này thì lấy bản cao nhất không vượt trần. */
        const val FEED_QUALITY_CAP = "720p"
        private const val STOP_TIMEOUT_MS = 5_000L
    }
}
