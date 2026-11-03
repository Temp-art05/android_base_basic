package com.base.app.feature.main.presentation.main.reels

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.base.app.R
import com.base.app.core.designsystem.component.GhostPillButton
import com.base.app.core.designsystem.component.GradientButton
import com.base.app.core.designsystem.component.KeyArt
import com.base.app.core.designsystem.ext.clickableNoRipple
import com.base.app.core.designsystem.icon.AppIcons
import com.base.app.core.designsystem.theme.AppTheme
import com.base.app.core.designsystem.theme.AppTypography
import com.base.app.core.media.KeepScreenOnWhilePlaying
import com.base.app.core.media.PlaybackProgress
import com.base.app.core.media.PlaybackSpeed
import com.base.app.core.media.PlaybackState
import com.base.app.core.media.VideoSlot
import com.base.app.core.media.VideoSurface
import com.base.app.core.ui.foundation.error.retryLabel
import com.base.app.core.ui.foundation.ext.CollectAsEffect
import com.base.app.core.utils.extension.findActivity
import com.base.app.feature.main.presentation.common.UiErrorState
import kotlin.math.abs
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

/** Overlay mờ hết khi trang lệch ~60% chiều cao. */
private const val OVERLAY_FADE = 1.6f

/** Tự sang reel kế: trượt chậm, mềm — spring mặc định giật. */
private const val AUTO_SCROLL_MS = 650

/**
 * SAMPLE (video) — tab Reels. [isSelected]: tab ẩn vẫn RESUMED (Main giữ mọi tab trong pager) nên phải gate
 * phát theo tab đang chọn, không dựa lifecycle.
 */
@Composable
internal fun ReelsScreen(
    isSelected: Boolean,
    onOpenPlayer: (videoId: String) -> Unit,
    viewModel: ReelsViewModel = koinViewModel(),
) {
    val viewState by viewModel.viewState.collectAsStateWithLifecycle()
    // Không `by`: chỉ thanh track đọc `.value` → tick vị trí không recompose cả màn.
    val progress = viewModel.playbackProgress.collectAsStateWithLifecycle()
    val slots by viewModel.player.slots.collectAsStateWithLifecycle()
    val pagerState = rememberPagerState(initialPage = viewState.current) { viewState.pages.size }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    viewModel.singleEvent.CollectAsEffect { event ->
        when (event) {
            is ReelsSingleEvent.ScrollToPage -> scope.launch {
                pagerState.animateScrollToPage(event.page, animationSpec = tween(AUTO_SCROLL_MS, easing = FastOutSlowInEasing))
            }
            is ReelsSingleEvent.OpenPlayer -> onOpenPlayer(event.videoId)
        }
    }
    LaunchedEffect(isSelected) { viewModel.processIntent(ReelsIntent.TabVisible(isSelected)) }
    LifecycleEventEffect(Lifecycle.Event.ON_START) { viewModel.processIntent(ReelsIntent.ScreenStarted) }
    LifecycleEventEffect(Lifecycle.Event.ON_STOP) {
        val changingConfigurations = context.findActivity()?.isChangingConfigurations == true
        viewModel.processIntent(ReelsIntent.ScreenStopped(changingConfigurations))
    }
    if (isSelected) KeepScreenOnWhilePlaying(viewModel.player)

    val hasPages = viewState.pages.isNotEmpty()
    LaunchedEffect(pagerState, hasPages) {
        if (!hasPages) return@LaunchedEffect
        // VM là nguồn sự thật (sau xoay máy / process death nhảy thẳng tới reel đang xem).
        if (pagerState.currentPage != viewState.current) pagerState.scrollToPage(viewState.current)
        snapshotFlow { pagerState.settledPage }.collect { viewModel.processIntent(ReelsIntent.PageSettled(it)) }
    }

    ReelsContent(
        viewState = viewState,
        pagerState = pagerState,
        progress = progress,
        slots = slots,
        onRetry = { viewModel.processIntent(ReelsIntent.Retry) },
        onRetryPlayback = { viewModel.processIntent(ReelsIntent.RetryPlayback) },
        onTogglePlay = { viewModel.processIntent(ReelsIntent.TogglePlay) },
        onToggleDescription = { viewModel.processIntent(ReelsIntent.ToggleDescription) },
        onOpenPlayerClick = { viewModel.processIntent(ReelsIntent.OpenPlayerClick) },
        onCycleSpeed = { viewModel.processIntent(ReelsIntent.CycleSpeed) },
    )
}

@Composable
private fun ReelsContent(
    viewState: ReelsViewState,
    pagerState: PagerState,
    progress: State<PlaybackProgress>,
    slots: Map<Int, VideoSlot>,
    onRetry: () -> Unit,
    onRetryPlayback: () -> Unit,
    onTogglePlay: () -> Unit,
    onToggleDescription: () -> Unit,
    onOpenPlayerClick: () -> Unit,
    onCycleSpeed: () -> Unit,
) {
    Box(Modifier.fillMaxSize().background(AppTheme.colors.media.background)) {
        if (viewState.pages.isNotEmpty()) {
            VerticalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                // Trang ±1 dựng sẵn để surface của player lân cận đã có khung hình khi vuốt nhẹ.
                beyondViewportPageCount = 1,
                key = { viewState.pages[it].videoId },
            ) { page ->
                val isCurrent = page == viewState.current
                val item = viewState.pages[page]
                // Chỉ trang hiện tại nhận playback/expanded: buffering, playWhenReady đổi không recompose trang ±1.
                ReelPageView(
                    page = item,
                    slot = slots[page],
                    isCurrent = isCurrent,
                    playback = if (isCurrent) viewState.playback else null,
                    speed = viewState.speed,
                    descriptionExpanded = isCurrent && viewState.descriptionExpanded,
                    // Theo id reel, không theo `current`: khung đầu sau khi lật, player còn báo vị trí reel trước.
                    progress = { progress.value.fractionFor(item.videoId) },
                    overlayAlpha = { (1f - abs(pagerState.getOffsetDistanceInPages(page)) * OVERLAY_FADE).coerceIn(0f, 1f) },
                    onRetryPlayback = onRetryPlayback,
                    onTogglePlay = onTogglePlay,
                    onToggleDescription = onToggleDescription,
                    onOpenPlayerClick = onOpenPlayerClick,
                    onCycleSpeed = onCycleSpeed,
                )
            }
        }
        // Scrim trên cố định cho status bar.
        Box(Modifier.fillMaxWidth().height(150.dp).background(AppTheme.colors.scrimTop))
        when {
            viewState.error != null -> UiErrorState(
                error = viewState.error,
                onRetry = onRetry,
                modifier = Modifier.background(MaterialTheme.colorScheme.background),
            )
            viewState.isLoading -> Spinner(Modifier.align(Alignment.Center))
        }
    }
}

/**
 * Một trang = một video, **tự chứa** video + scrim + rail + mô tả + nút + track: vuốt là cả trang trượt,
 * overlay mờ dần ([overlayAlpha], đọc trong `graphicsLayer` → không recompose mỗi frame). Trang lân cận vẽ
 * surface của player đã prepare + pause; chưa có khung hình thì key art.
 */
@Composable
private fun ReelPageView(
    page: ReelPage,
    slot: VideoSlot?,
    isCurrent: Boolean,
    /** null với trang không phải trang hiện tại. */
    playback: PlaybackState?,
    speed: Float,
    descriptionExpanded: Boolean,
    progress: () -> Float,
    overlayAlpha: () -> Float,
    onRetryPlayback: () -> Unit,
    onTogglePlay: () -> Unit,
    onToggleDescription: () -> Unit,
    onOpenPlayerClick: () -> Unit,
    onCycleSpeed: () -> Unit,
) {
    val art: @Composable () -> Unit = {
        KeyArt(posterUrl = page.thumbnailUrl, seed = page.videoId, modifier = Modifier.fillMaxSize())
    }
    BoxWithConstraints(Modifier.fillMaxSize()) {
        // Khung ngang (xoay máy): vừa khung — crop video dọc lên khung ngang chỉ còn một dải giữa hình.
        val videoScale = if (maxWidth > maxHeight) ContentScale.Fit else ContentScale.Crop
        if (slot != null) VideoSurface(slot = slot, modifier = Modifier.fillMaxSize(), contentScale = videoScale, shutter = art) else art()
        Box(Modifier.align(Alignment.BottomCenter).fillMaxWidth().fillMaxHeight(0.46f).background(AppTheme.colors.scrimBottom))
        if (playback != null) {
            val label = stringResource(if (playback.playWhenReady) R.string.video_pause_cd else R.string.video_play_cd)
            Box(
                Modifier
                    .fillMaxSize()
                    .clickableNoRipple(onClickLabel = label, role = Role.Button, onClick = onTogglePlay)
                    .semantics { contentDescription = label },
            )
        }
        Box(Modifier.fillMaxSize().graphicsLayer { alpha = overlayAlpha() }) {
            when {
                playback == null -> Unit
                playback.error != null -> CenterAction(
                    title = stringResource(R.string.video_error_playback),
                    action = retryLabel(),
                    onAction = onRetryPlayback,
                )
                playback.isBuffering && playback.playWhenReady -> Spinner(Modifier.align(Alignment.Center))
                !playback.playWhenReady -> PlayBadge(Modifier.align(Alignment.Center))
            }
            ReelOverlay(
                page = page,
                isCurrent = isCurrent,
                speed = speed,
                expanded = descriptionExpanded,
                progress = progress,
                onToggleDescription = onToggleDescription,
                onOpenPlayerClick = onOpenPlayerClick,
                onCycleSpeed = onCycleSpeed,
            )
        }
    }
}

@Composable
private fun BoxScope.ReelOverlay(
    page: ReelPage,
    isCurrent: Boolean,
    speed: Float,
    expanded: Boolean,
    progress: () -> Float,
    onToggleDescription: () -> Unit,
    onOpenPlayerClick: () -> Unit,
    onCycleSpeed: () -> Unit,
) {
    val media = AppTheme.colors.media
    Column(
        Modifier
            .align(Alignment.BottomCenter)
            .fillMaxWidth()
            .padding(start = AppTheme.dimensions.s4, end = AppTheme.dimensions.s3, bottom = 18.dp),
    ) {
        Row(verticalAlignment = Alignment.Bottom) {
            Column(Modifier.weight(1f).padding(end = AppTheme.dimensions.s4)) {
                Text(
                    text = page.title,
                    style = shadowed(AppTypography.titleSmall.copy(fontSize = 16.sp)),
                    color = media.text,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                if (page.description.isNotBlank()) {
                    Text(
                        text = page.description,
                        style = shadowed(AppTypography.label.copy(fontWeight = FontWeight.Normal, lineHeight = 19.5.sp)),
                        color = media.textBody,
                        maxLines = if (expanded) EXPANDED_LINES else COLLAPSED_LINES,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .padding(top = 6.dp)
                            .clickableNoRipple(onClick = { if (isCurrent) onToggleDescription() }),
                    )
                }
                GradientButton(
                    text = stringResource(R.string.video_open_player),
                    onClick = { if (isCurrent) onOpenPlayerClick() },
                    modifier = Modifier.padding(top = AppTheme.dimensions.s3).height(40.dp),
                    leadingIcon = {
                        Icon(AppIcons.Play, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(16.dp))
                    },
                )
            }
            SpeedRailItem(
                speed = speed,
                onClick = { if (isCurrent) onCycleSpeed() },
                modifier = Modifier.padding(bottom = AppTheme.dimensions.s5),
            )
        }
        ReelTrack(progress = progress, modifier = Modifier.padding(top = 18.dp))
    }
}

/** "1×" — chạm đổi tốc độ 1 → 1.5 → 2. */
@Composable
private fun SpeedRailItem(speed: Float, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val description = stringResource(R.string.video_speed_cd, PlaybackSpeed.label(speed))
    Column(
        modifier = modifier
            .defaultMinSize(minWidth = AppTheme.dimensions.touchTarget, minHeight = AppTheme.dimensions.touchTarget)
            .clip(AppTheme.shapes.md)
            .clickable(role = Role.Button, onClick = onClick)
            // Nhãn dài (bản dịch) chạm góc bo của clip → bị cắt 2 bên; chừa đệm trong vùng clip.
            .padding(horizontal = 6.dp)
            .semantics { contentDescription = description },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Text(
            text = stringResource(R.string.video_speed_value, PlaybackSpeed.label(speed)),
            style = shadowed(AppTypography.titleSmall.copy(fontWeight = FontWeight.ExtraBold, lineHeight = 29.sp)),
            color = AppTheme.colors.media.text,
            modifier = Modifier.height(AppTheme.dimensions.railIcon),
        )
        Text(
            text = stringResource(R.string.video_speed),
            style = shadowed(AppTypography.captionSemibold.copy(fontSize = 12.sp)),
            color = AppTheme.colors.media.text,
        )
    }
}

/** Track 3dp, chỉ hiển thị (vào Player để tua). Vẽ ở pha draw: tick vị trí không recompose. */
@Composable
private fun ReelTrack(progress: () -> Float, modifier: Modifier = Modifier) {
    val track = AppTheme.colors.media.track
    val gradient = AppTheme.colors.gradientCta
    Box(
        modifier
            .fillMaxWidth()
            .height(3.dp)
            .drawBehind {
                val radius = CornerRadius(size.height / 2)
                drawRoundRect(track, cornerRadius = radius)
                val fraction = progress()
                if (fraction > 0f) drawRoundRect(gradient, size = size.copy(width = size.width * fraction), cornerRadius = radius)
            },
    )
}

@Composable
private fun Spinner(modifier: Modifier = Modifier) {
    CircularProgressIndicator(
        color = AppTheme.colors.media.text,
        trackColor = AppTheme.colors.media.spinnerTrack,
        strokeWidth = 2.5.dp,
        modifier = modifier.size(44.dp),
    )
}

@Composable
private fun PlayBadge(modifier: Modifier = Modifier) {
    Box(modifier.size(64.dp).background(AppTheme.colors.media.glass, AppTheme.shapes.full), contentAlignment = Alignment.Center) {
        Icon(AppIcons.Play, contentDescription = null, tint = AppTheme.colors.media.text, modifier = Modifier.size(32.dp))
    }
}

@Composable
private fun BoxScope.CenterAction(title: String, action: String, onAction: () -> Unit) {
    Column(Modifier.align(Alignment.Center).padding(horizontal = 40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(title, style = AppTypography.titleSmall, color = AppTheme.colors.media.text, textAlign = TextAlign.Center)
        GhostPillButton(text = action, onClick = onAction, modifier = Modifier.padding(top = AppTheme.dimensions.s4))
    }
}

@Composable
private fun shadowed(style: TextStyle): TextStyle =
    style.copy(shadow = Shadow(color = AppTheme.colors.media.textShadow, blurRadius = 6f))

private const val COLLAPSED_LINES = 2
private const val EXPANDED_LINES = 8

@Preview(widthDp = 360, heightDp = 760)
@Composable
private fun ReelsPreview() {
    AppTheme {
        ReelsContent(
            viewState = ReelsViewState(
                isLoading = false,
                pages = listOf(
                    ReelPage(
                        videoId = "big-buck-bunny",
                        title = "Big Buck Bunny",
                        description = "Blender open movie at 320×180, about 10 minutes — small file, decodes everywhere.",
                        thumbnailUrl = null,
                    ),
                ),
            ),
            pagerState = rememberPagerState { 1 },
            progress = remember { mutableStateOf(PlaybackProgress(itemId = "big-buck-bunny", positionMs = 30_000, durationMs = 100_000)) },
            slots = emptyMap(),
            onRetry = {},
            onRetryPlayback = {},
            onTogglePlay = {},
            onToggleDescription = {},
            onOpenPlayerClick = {},
            onCycleSpeed = {},
        )
    }
}
