package com.base.app.feature.main.presentation.player

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.base.app.R
import com.base.app.core.designsystem.component.AppBottomSheet
import com.base.app.core.designsystem.component.GhostPillButton
import com.base.app.core.designsystem.component.KeyArt
import com.base.app.core.designsystem.ext.clickableNoRipple
import com.base.app.core.designsystem.icon.AppIcons
import com.base.app.core.designsystem.theme.AppTheme
import com.base.app.core.designsystem.theme.AppTypography
import com.base.app.core.media.KeepScreenOnWhilePlaying
import com.base.app.core.media.PlaybackProgress
import com.base.app.core.media.PlaybackState
import com.base.app.core.media.VideoSlot
import com.base.app.core.media.VideoSurface
import com.base.app.core.ui.foundation.error.retryLabel
import com.base.app.core.ui.foundation.ext.CollectAsEffect
import com.base.app.core.utils.extension.findActivity
import com.base.app.feature.main.presentation.common.UiErrorState
import com.base.app.feature.main.presentation.player.component.CenterPlayIcon
import com.base.app.feature.main.presentation.player.component.PlaybackOptionsSheet
import com.base.app.feature.main.presentation.player.component.PlayerInfo
import com.base.app.feature.main.presentation.player.component.PlayerRail
import com.base.app.feature.main.presentation.player.component.PlayerTopBar
import com.base.app.feature.main.presentation.player.component.PlayerTrack
import kotlin.math.abs
import kotlinx.coroutines.launch

/** Overlay mờ hết khi trang lệch ~60% chiều cao. */
private const val OVERLAY_FADE = 1.6f

/** Trượt sang video kế khi tự chuyển: đủ chậm để mắt theo kịp, không giật như spring mặc định. */
private const val AUTO_SCROLL_MS = 650

/** SAMPLE (video) — trình phát toàn màn, vuốt dọc qua cả feed. */
@Composable
fun PlayerScreen(
    viewModel: PlayerViewModel,
    onBack: () -> Unit,
) {
    val viewState by viewModel.viewState.collectAsStateWithLifecycle()
    // Không `by`: chỉ thanh track đọc `.value` → tick vị trí không recompose cả màn.
    val progress = viewModel.playbackProgress.collectAsStateWithLifecycle()
    val slots by viewModel.player.slots.collectAsStateWithLifecycle()
    val pagerState = rememberPagerState { viewState.pages.size }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    viewModel.singleEvent.CollectAsEffect { event ->
        when (event) {
            PlayerSingleEvent.NavigateBack -> onBack()
            is PlayerSingleEvent.ScrollToPage -> scope.launch {
                // Trang liền kề: trượt chậm, mềm; xa hơn thì nhảy thẳng.
                if (abs(event.page - pagerState.currentPage) == 1) {
                    pagerState.animateScrollToPage(event.page, animationSpec = tween(AUTO_SCROLL_MS, easing = FastOutSlowInEasing))
                } else {
                    pagerState.scrollToPage(event.page)
                }
            }
        }
    }
    // Vào nền → pause + lưu vị trí; trở lại → phát tiếp nếu trước đó đang phát. Quyết định ở VM.
    LifecycleEventEffect(Lifecycle.Event.ON_START) { viewModel.processIntent(PlayerIntent.ScreenStarted) }
    LifecycleEventEffect(Lifecycle.Event.ON_STOP) {
        val changingConfigurations = context.findActivity()?.isChangingConfigurations == true
        viewModel.processIntent(PlayerIntent.ScreenStopped(changingConfigurations))
    }
    BackHandler { viewModel.processIntent(PlayerIntent.BackClick) }
    KeepScreenOnWhilePlaying(viewModel.player)

    val pagesLoaded = viewState.pages.isNotEmpty()
    LaunchedEffect(pagerState, pagesLoaded) {
        if (!pagesLoaded) return@LaunchedEffect
        // VM là nguồn sự thật: lần đầu có trang (hoặc sau xoay máy) nhảy thẳng tới video hiện tại.
        if (pagerState.currentPage != viewState.current) pagerState.scrollToPage(viewState.current)
        snapshotFlow { pagerState.settledPage }.collect { viewModel.processIntent(PlayerIntent.PageSettled(it)) }
    }

    Box(Modifier.fillMaxSize().background(AppTheme.colors.media.background)) {
        PlayerContent(
            viewState = viewState,
            pagerState = pagerState,
            progress = { progress.value },
            slots = slots,
            onBackClick = { viewModel.processIntent(PlayerIntent.BackClick) },
            onRetryClick = { viewModel.processIntent(PlayerIntent.RetryClick) },
            onTogglePlay = { viewModel.processIntent(PlayerIntent.TogglePlay) },
            onToggleDescription = { viewModel.processIntent(PlayerIntent.ToggleDescription) },
            onSwitchBackToHigherQuality = { viewModel.processIntent(PlayerIntent.SwitchBackToHigherQuality) },
            onReplayClick = { viewModel.processIntent(PlayerIntent.ReplayClick) },
            onSpeedClick = { viewModel.processIntent(PlayerIntent.SpeedClick) },
            onOptionsClick = { viewModel.processIntent(PlayerIntent.OptionsClick) },
            onSeek = { positionMs -> viewModel.processIntent(PlayerIntent.SeekTo(positionMs)) },
        )
        AppBottomSheet(
            visible = viewState.optionsVisible,
            onDismiss = { viewModel.processIntent(PlayerIntent.DismissOptions) },
        ) {
            PlaybackOptionsSheet(
                speed = viewState.speed,
                qualities = viewState.qualities,
                quality = viewState.quality,
                onSpeedSelect = { speed -> viewModel.processIntent(PlayerIntent.SpeedSelect(speed)) },
                onAutoQualitySelect = { viewModel.processIntent(PlayerIntent.QualitySelect(null)) },
                onQualitySelect = { quality -> viewModel.processIntent(PlayerIntent.QualitySelect(quality)) },
            )
        }
    }
}

@Composable
private fun PlayerContent(
    viewState: PlayerViewState,
    pagerState: PagerState,
    progress: () -> PlaybackProgress,
    slots: Map<Int, VideoSlot>,
    onBackClick: () -> Unit,
    onRetryClick: () -> Unit,
    onTogglePlay: () -> Unit,
    onToggleDescription: () -> Unit,
    onSwitchBackToHigherQuality: () -> Unit,
    onReplayClick: () -> Unit,
    onSpeedClick: () -> Unit,
    onOptionsClick: () -> Unit,
    onSeek: (positionMs: Long) -> Unit,
) {
    Box(Modifier.fillMaxSize()) {
        if (viewState.pages.isNotEmpty()) {
            VerticalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                // Trang ±1 dựng sẵn để surface của player lân cận đã có khung hình khi vuốt nhẹ.
                beyondViewportPageCount = 1,
                key = { viewState.pages[it].videoId },
            ) { page ->
                VideoPage(
                    page = viewState.pages[page],
                    slot = slots[page],
                    isCurrent = page == viewState.current,
                    viewState = viewState,
                    progress = progress,
                    // Overlay mờ dần theo độ lệch trang; đọc trong lambda của graphicsLayer → không recompose mỗi frame.
                    overlayAlpha = { (1f - abs(pagerState.getOffsetDistanceInPages(page)) * OVERLAY_FADE).coerceIn(0f, 1f) },
                    onRetryClick = onRetryClick,
                    onTogglePlay = onTogglePlay,
                    onToggleDescription = onToggleDescription,
                    onSwitchBackToHigherQuality = onSwitchBackToHigherQuality,
                    onReplayClick = onReplayClick,
                    onSpeedClick = onSpeedClick,
                    onOptionsClick = onOptionsClick,
                    onSeek = onSeek,
                )
            }
        }

        // Top bar cố định: không trượt theo trang, đổi tên khi trang dừng.
        Box(Modifier.fillMaxWidth().height(150.dp).background(AppTheme.colors.scrimTop))
        PlayerTopBar(
            title = viewState.pages.getOrNull(viewState.current)?.title.orEmpty(),
            position = viewState.current + 1,
            total = viewState.pages.size,
            onBack = onBackClick,
            modifier = Modifier
                .statusBarsPadding()
                .padding(top = AppTheme.dimensions.s2, end = AppTheme.dimensions.s3),
        )

        when {
            viewState.error != null -> UiErrorState(
                error = viewState.error,
                onRetry = onRetryClick,
                modifier = Modifier.padding(top = 120.dp),
            )
            viewState.isLoading -> CenterSpinner()
        }
    }
}

/**
 * Một trang của pager = một video, **tự chứa** video + scrim + mô tả + rail + track: vuốt là cả trang trượt
 * theo, overlay mờ dần ([overlayAlpha]). Trang lân cận vẽ surface của player đã prepare + pause (khung hình
 * thật); chưa có khung hình thì key art. Chỉ trang hiện tại nhận tap play/pause, đọc vị trí live, hiện buffering.
 */
@Composable
private fun VideoPage(
    page: PlayerPage,
    slot: VideoSlot?,
    isCurrent: Boolean,
    viewState: PlayerViewState,
    progress: () -> PlaybackProgress,
    overlayAlpha: () -> Float,
    onRetryClick: () -> Unit,
    onTogglePlay: () -> Unit,
    onToggleDescription: () -> Unit,
    onSwitchBackToHigherQuality: () -> Unit,
    onReplayClick: () -> Unit,
    onSpeedClick: () -> Unit,
    onOptionsClick: () -> Unit,
    onSeek: (positionMs: Long) -> Unit,
) {
    val media = AppTheme.colors.media
    val playback = viewState.playback
    val active = isCurrent && !viewState.isLoading && viewState.error == null
    val buffering = active && playback.isBuffering && playback.playWhenReady
    val art: @Composable () -> Unit = {
        KeyArt(posterUrl = page.thumbnailUrl, seed = page.videoId, modifier = Modifier.fillMaxSize())
    }

    BoxWithConstraints(Modifier.fillMaxSize()) {
        // Khung ngang (xoay máy): vừa khung, viền đen hai bên — crop thì chỉ còn một dải giữa hình.
        val videoScale = if (maxWidth > maxHeight) ContentScale.Fit else ContentScale.Crop
        if (slot != null) {
            VideoSurface(slot = slot, modifier = Modifier.fillMaxSize(), contentScale = videoScale, shutter = art)
        } else {
            art()
        }
        if (buffering) Box(Modifier.fillMaxSize().background(media.bufferingDim))
        Box(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .fillMaxHeight(0.46f)
                .background(AppTheme.colors.scrimBottom),
        )
        if (active) {
            // Chạm vào video = play/pause; TalkBack đọc được hành động.
            val label = stringResource(if (playback.playWhenReady) R.string.video_pause_cd else R.string.video_play_cd)
            Box(
                Modifier
                    .fillMaxSize()
                    .clickableNoRipple(onClickLabel = label, role = Role.Button, onClick = onTogglePlay)
                    .semantics { contentDescription = label },
            )
        }
        val expanded = active && viewState.descriptionExpanded
        if (expanded) {
            // Mô tả mở rộng: nền tối hơn để đọc; chạm ra ngoài thì thu lại.
            Box(
                Modifier
                    .fillMaxSize()
                    .background(media.bufferingDim)
                    .clickableNoRipple(onClick = onToggleDescription),
            )
        }

        Box(Modifier.fillMaxSize().graphicsLayer { alpha = overlayAlpha() }) {
            when {
                !active -> Unit
                viewState.sourceUnavailable -> CenterMessage(title = stringResource(R.string.video_error_no_source))
                viewState.hasPlaybackError -> CenterMessage(
                    title = stringResource(R.string.video_error_playback),
                    actionLabel = retryLabel(),
                    onAction = onRetryClick,
                )
                viewState.feedEnded -> CenterMessage(
                    title = stringResource(R.string.player_feed_end),
                    actionLabel = stringResource(R.string.player_replay),
                    onAction = onReplayClick,
                )
                buffering -> BufferingOverlay(viewState.qualityDropped, onSwitchBack = onSwitchBackToHigherQuality)
                !playback.playWhenReady -> CenterPlayIcon(AppIcons.Play, Modifier.align(Alignment.Center))
            }
            if (!viewState.isLoading && viewState.error == null) {
                BottomControls(
                    page = page,
                    isCurrent = isCurrent,
                    speed = viewState.speed,
                    expanded = expanded,
                    // Theo id video, không theo `isCurrent`: khung đầu sau khi lật, player còn báo số đo của video trước.
                    positionMs = { progress().positionFor(page.videoId) },
                    durationMs = if (playback.itemId == page.videoId) playback.durationMs else 0,
                    buffering = buffering,
                    onToggleDescription = onToggleDescription,
                    onSpeedClick = onSpeedClick,
                    onOptionsClick = onOptionsClick,
                    onSeek = onSeek,
                )
            }
        }
    }
}

@Composable
private fun BoxScope.BottomControls(
    page: PlayerPage,
    isCurrent: Boolean,
    speed: Float,
    expanded: Boolean,
    positionMs: () -> Long,
    durationMs: Long,
    buffering: Boolean,
    onToggleDescription: () -> Unit,
    onSpeedClick: () -> Unit,
    onOptionsClick: () -> Unit,
    onSeek: (positionMs: Long) -> Unit,
) {
    // Đang buffering chỉ còn top bar + spinner + track mờ. Mờ dần thay vì gỡ khỏi layout: track không nhảy chỗ,
    // lần buffer ngắn (tua) không làm cả cụm chớp.
    val chromeAlpha by animateFloatAsState(if (buffering) 0f else 1f, label = "bufferingChrome")
    Column(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = AppTheme.dimensions.screenGutter)
            .padding(bottom = 14.dp),
    ) {
        Row(verticalAlignment = Alignment.Bottom, modifier = Modifier.graphicsLayer { alpha = chromeAlpha }) {
            PlayerInfo(
                description = page.description,
                expanded = expanded,
                onToggle = { if (isCurrent && !buffering) onToggleDescription() },
                modifier = Modifier
                    .weight(1f)
                    .padding(end = AppTheme.dimensions.s4),
            )
            PlayerRail(
                speed = speed,
                onSpeedClick = { if (isCurrent && !buffering) onSpeedClick() },
                onSpeedLongPress = { if (isCurrent && !buffering) onOptionsClick() },
                onOptionsClick = { if (isCurrent && !buffering) onOptionsClick() },
                modifier = Modifier.padding(bottom = AppTheme.dimensions.s6),
            )
        }
        PlayerTrack(
            positionMs = positionMs,
            durationMs = durationMs,
            dimmed = buffering,
            seekable = isCurrent,
            onSeek = onSeek,
            modifier = Modifier.padding(top = AppTheme.dimensions.s3),
        )
    }
}

/** Spinner + "Loading…"; dòng hạ quality chỉ khi Auto thật sự đã hạ. */
@Composable
private fun BoxScope.BufferingOverlay(qualityDropped: String?, onSwitchBack: () -> Unit) {
    val media = AppTheme.colors.media
    Column(
        modifier = Modifier
            .align(Alignment.Center)
            .padding(horizontal = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator(
            color = media.text,
            trackColor = media.spinnerTrack,
            strokeWidth = 2.5.dp,
            modifier = Modifier.size(44.dp),
        )
        Text(
            text = stringResource(R.string.player_loading),
            style = AppTypography.bodySemibold.copy(fontSize = 14.5.sp),
            color = media.text,
            modifier = Modifier.padding(top = 18.dp),
        )
        if (qualityDropped != null) {
            Text(
                text = stringResource(R.string.player_quality_dropped, qualityDropped),
                style = AppTypography.caption.copy(fontSize = 12.5.sp),
                color = media.textMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 7.dp),
            )
            GhostPillButton(
                text = stringResource(R.string.player_switch_back_quality),
                onClick = onSwitchBack,
                modifier = Modifier.padding(top = AppTheme.dimensions.s4),
            )
        }
    }
}

@Composable
private fun BoxScope.CenterSpinner() {
    CircularProgressIndicator(
        color = AppTheme.colors.media.text,
        trackColor = AppTheme.colors.media.spinnerTrack,
        strokeWidth = 2.5.dp,
        modifier = Modifier.align(Alignment.Center).size(44.dp),
    )
}

@Composable
private fun BoxScope.CenterMessage(
    title: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Column(
        modifier = Modifier
            .align(Alignment.Center)
            .padding(horizontal = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppTheme.dimensions.s2),
    ) {
        Text(
            text = title,
            style = AppTypography.titleSmall,
            color = AppTheme.colors.media.text,
            textAlign = TextAlign.Center,
        )
        if (actionLabel != null && onAction != null) {
            GhostPillButton(text = actionLabel, onClick = onAction, modifier = Modifier.padding(top = AppTheme.dimensions.s2))
        }
    }
}

@Preview(widthDp = 360, heightDp = 760)
@Composable
private fun PlayerContentPreview() {
    AppTheme {
        Box(Modifier.background(MaterialTheme.colorScheme.background)) {
            PlayerContent(
                viewState = PlayerViewState(
                    isLoading = false,
                    pages = listOf(
                        PlayerPage(
                            videoId = "frame-counter",
                            title = "Frame counter (1 hour)",
                            description = "One-hour frame counter. Seek ahead, leave the player and come back: it resumes from where you stopped.",
                            thumbnailUrl = null,
                        ),
                    ),
                    playback = PlaybackState(itemId = "frame-counter", isPlaying = true, playWhenReady = true, durationMs = 3_600_000),
                ),
                pagerState = rememberPagerState { 1 },
                progress = { PlaybackProgress(itemId = "frame-counter", positionMs = 420_000, durationMs = 3_600_000) },
                slots = emptyMap(),
                onBackClick = {},
                onRetryClick = {},
                onTogglePlay = {},
                onToggleDescription = {},
                onSwitchBackToHigherQuality = {},
                onReplayClick = {},
                onSpeedClick = {},
                onOptionsClick = {},
                onSeek = {},
            )
        }
    }
}
