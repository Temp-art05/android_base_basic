package com.base.app.core.media

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

/**
 * Player giả: ghi lại lệnh, test tự đẩy [PlaybackState] qua [emit]. Mỗi `setCurrent` vào mục có URL
 * được ghi thành một [Load] (URL lấy từ playlist) — giống "nạp nguồn" ở player thật.
 */
class FakeFeedPlayer : EpisodeFeedPlayer {
    data class Load(val url: String, val startPositionMs: Long, val playWhenReady: Boolean)

    private val _state = MutableStateFlow(PlaybackState())
    override val state: StateFlow<PlaybackState> = _state
    override val slots: StateFlow<Map<Int, VideoSlot>> = MutableStateFlow(emptyMap())

    var items: List<FeedItem> = emptyList()
        private set
    val loads = mutableListOf<Load>()

    /** Vị trí playlist của mọi lần `setCurrent`, kể cả mục khoá. */
    val currents = mutableListOf<Int>()
    var isPreloadActive = true
        private set
    var isActive = true
        private set
    var isMuted = false
        private set

    /** Lịch sử `setActive` theo thứ tự gọi. */
    val activeCalls = mutableListOf<Boolean>()
    var lastSpeed = 1f
    var released = false
    var position = 0L

    private var current = -1

    override fun setPlaylist(items: List<FeedItem>) {
        // Như player thật: URL của tập đang phát đổi → bản đã nạp bị giải phóng, vị trí về 0.
        if (current >= 0 && items.getOrNull(current)?.url != this.items.getOrNull(current)?.url) position = 0
        this.items = items
    }

    override fun setCurrent(position: Int, startPositionMs: Long, playWhenReady: Boolean) {
        currents += position
        current = position
        val url = items[position].url
        if (url == null) {
            _state.value = PlaybackState()
            return
        }
        loads += Load(url, startPositionMs, playWhenReady)
        this.position = startPositionMs
        _state.value = PlaybackState(
            itemId = items[position].id,
            positionMs = startPositionMs,
            playWhenReady = playWhenReady,
            isBuffering = true,
        )
    }

    override fun setPreloadActive(active: Boolean) {
        isPreloadActive = active
    }

    override fun setActive(active: Boolean) {
        activeCalls += active
        isActive = active
        if (!active) _state.update { it.copy(isPlaying = false, playWhenReady = false) }
    }

    override fun setMuted(muted: Boolean) {
        isMuted = muted
    }

    override fun play() = _state.update { it.copy(playWhenReady = true, isPlaying = true, ended = false) }

    override fun pause() = _state.update { it.copy(playWhenReady = false, isPlaying = false) }

    override fun seekTo(positionMs: Long) {
        position = positionMs
        _state.update { it.copy(positionMs = positionMs) }
    }

    override fun setSpeed(speed: Float) {
        lastSpeed = speed
    }

    override fun currentPositionMs(): Long = position

    override fun release() {
        released = true
    }

    /** Giả lập player đang phát ở [positionMs] / [durationMs]. */
    fun playing(positionMs: Long, durationMs: Long = 100_000) {
        position = positionMs
        _state.value = PlaybackState(
            itemId = _state.value.itemId,
            isPlaying = true,
            playWhenReady = true,
            positionMs = positionMs,
            durationMs = durationMs,
        )
    }

    fun emit(state: PlaybackState) {
        position = state.positionMs
        _state.value = state
    }
}
