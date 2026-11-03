package com.base.app.core.media

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.compose.ContentFrame
import androidx.media3.ui.compose.SURFACE_TYPE_SURFACE_VIEW
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/**
 * Bề mặt vẽ video của một player trong pool, phủ kín khung (crop) như mockup: video dọc 9:16 trên
 * màn 20:9 chỉ mất ~10% hai bên thay vì viền đen trên dưới. [shutter] (poster) che tới khi player có
 * khung hình đầu. Slot không phải của [ExoEpisodeFeedPlayer] (preview, test) thì chỉ hiện [shutter].
 * Màn ngang: caller truyền [ContentScale.Fit] — crop video dọc lên khung ngang cắt mất gần hết hình.
 */
@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun VideoSurface(
    slot: VideoSlot,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    shutter: @Composable () -> Unit = {},
) {
    val exoPlayer = (slot as? ExoEpisodeFeedPlayer.Slot)?.exoPlayer ?: return shutter()
    ContentFrame(
        player = exoPlayer,
        modifier = modifier,
        surfaceType = SURFACE_TYPE_SURFACE_VIEW,
        contentScale = contentScale,
        shutter = shutter,
    )
}

/** Màn sáng suốt lúc tập hiện tại đang phát. Chỉ đọc `isPlaying` — tick vị trí không recompose. */
@Composable
fun KeepScreenOnWhilePlaying(player: EpisodeFeedPlayer) {
    val isPlaying by remember(player) { player.state.map { it.isPlaying }.distinctUntilChanged() }
        .collectAsStateWithLifecycle(initialValue = false)
    val view = LocalView.current
    DisposableEffect(view, isPlaying) {
        view.keepScreenOn = isPlaying
        onDispose { view.keepScreenOn = false }
    }
}
