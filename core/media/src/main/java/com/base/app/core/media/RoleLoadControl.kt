package com.base.app.core.media

import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.LoadControl
import androidx.media3.exoplayer.analytics.PlayerId
import androidx.media3.exoplayer.source.TrackGroupArray
import androidx.media3.exoplayer.trackselection.ExoTrackSelection
import androidx.media3.exoplayer.upstream.Allocator

/**
 * Load control theo vai của player trong pool (bọc `DefaultLoadControl` dựng theo [MediaProfile]).
 * Player hiện tại tải như thường; player lân cận chỉ tải tới [neighborCapMs] rồi dừng, và **chưa
 * tải gì** khi [neighborLoadingAllowed] còn false (tập hiện tại chưa lên hình — không giành băng
 * thông). Hai cờ do main thread đổi, playback thread đọc.
 */
@OptIn(UnstableApi::class)
internal class RoleLoadControl(profile: MediaProfile) : LoadControl {
    private val delegate: DefaultLoadControl = DefaultLoadControl.Builder()
        .setBufferDurationsMs(
            profile.minBufferMs,
            profile.maxBufferMs,
            profile.bufferForPlaybackMs,
            minOf(REBUFFER_MS, profile.minBufferMs),
        )
        .apply { profile.targetBufferBytes?.let(::setTargetBufferBytes) }
        .build()

    /** null = player hiện tại. */
    @Volatile
    var neighborCapMs: Long? = null

    @Volatile
    var neighborLoadingAllowed: Boolean = false

    override fun shouldContinueLoading(parameters: LoadControl.Parameters): Boolean {
        val cap = neighborCapMs ?: return delegate.shouldContinueLoading(parameters)
        return neighborLoadingAllowed && parameters.bufferedDurationUs < cap * 1_000
    }

    override fun shouldStartPlayback(parameters: LoadControl.Parameters): Boolean = delegate.shouldStartPlayback(parameters)

    override fun onPrepared(playerId: PlayerId) = delegate.onPrepared(playerId)

    override fun onTracksSelected(
        parameters: LoadControl.Parameters,
        trackGroups: TrackGroupArray,
        trackSelections: Array<out ExoTrackSelection?>,
    ) = delegate.onTracksSelected(parameters, trackGroups, trackSelections)

    override fun onStopped(playerId: PlayerId) = delegate.onStopped(playerId)

    override fun onReleased(playerId: PlayerId) = delegate.onReleased(playerId)

    override fun getAllocator(playerId: PlayerId): Allocator = delegate.getAllocator(playerId)

    override fun getBackBufferDurationUs(playerId: PlayerId): Long = delegate.getBackBufferDurationUs(playerId)

    override fun retainBackBufferFromKeyframe(playerId: PlayerId): Boolean = delegate.retainBackBufferFromKeyframe(playerId)

    private companion object {
        const val REBUFFER_MS = 1_500
    }
}
