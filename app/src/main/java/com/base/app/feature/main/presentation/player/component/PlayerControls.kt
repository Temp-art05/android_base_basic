package com.base.app.feature.main.presentation.player.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.base.app.R
import com.base.app.core.designsystem.ext.applyIf
import com.base.app.core.designsystem.icon.AppIcons
import com.base.app.core.designsystem.theme.AppTheme
import com.base.app.core.designsystem.theme.AppTypography
import com.base.app.core.media.PlaybackSpeed
import java.util.Locale

@Composable
private fun shadowed(style: TextStyle): TextStyle =
    style.copy(shadow = Shadow(color = AppTheme.colors.media.textShadow, blurRadius = 6f))

/** Back + tên video + "N / M" (vị trí trong feed). */
@Composable
internal fun PlayerTopBar(
    title: String,
    position: Int,
    total: Int,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val media = AppTheme.colors.media
    Row(
        modifier = modifier.padding(horizontal = AppTheme.dimensions.s1),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppTheme.dimensions.s1),
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.common_back),
                tint = media.text,
            )
        }
        Column {
            Text(
                text = title,
                style = shadowed(AppTypography.button),
                color = media.text,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (total > 0) {
                Text(
                    text = stringResource(R.string.player_position, position, total),
                    style = shadowed(AppTypography.caption),
                    color = media.textSecondary,
                    modifier = Modifier.padding(top = 1.dp),
                )
            }
        }
    }
}

/** Rail phải: "1×" (chạm = đổi tốc độ, nhấn giữ = sheet tuỳ chọn) và nút tuỳ chọn phát. */
@Composable
internal fun PlayerRail(
    speed: Float,
    onSpeedClick: () -> Unit,
    onSpeedLongPress: () -> Unit,
    onOptionsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(AppTheme.dimensions.s5),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        RailItem(
            label = stringResource(R.string.video_speed),
            contentDescription = stringResource(R.string.video_speed_cd, PlaybackSpeed.label(speed)),
            onClick = onSpeedClick,
            onLongClick = onSpeedLongPress,
        ) {
            Text(
                text = stringResource(R.string.video_speed_value, PlaybackSpeed.label(speed)),
                style = shadowed(AppTypography.titleSmall.copy(fontWeight = FontWeight.ExtraBold, lineHeight = 29.sp)),
                color = AppTheme.colors.media.text,
                modifier = Modifier.height(AppTheme.dimensions.railIcon),
            )
        }
        RailItem(
            label = stringResource(R.string.player_options),
            contentDescription = null,
            onClick = onOptionsClick,
        ) {
            Icon(
                imageVector = AppIcons.More,
                contentDescription = null,
                tint = AppTheme.colors.media.text,
                modifier = Modifier.size(AppTheme.dimensions.railIcon),
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun RailItem(
    label: String,
    contentDescription: String?,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    icon: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier
            .defaultMinSize(minWidth = AppTheme.dimensions.touchTarget, minHeight = AppTheme.dimensions.touchTarget)
            .clip(AppTheme.shapes.md)
            .combinedClickable(role = Role.Button, onClick = onClick, onLongClick = onLongClick)
            // Nhãn dài (bản dịch) chạm góc bo của clip → bị cắt 2 bên; chừa đệm trong vùng clip.
            .padding(horizontal = 6.dp)
            .applyIf(contentDescription != null) { semantics { this.contentDescription = contentDescription.orEmpty() } },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        icon()
        Text(
            text = label,
            style = shadowed(AppTypography.captionSemibold.copy(fontSize = 11.sp)),
            color = AppTheme.colors.media.text,
        )
    }
}

/**
 * Mô tả 2 dòng + "more". "more" chỉ hiện khi mô tả thật sự bị cắt; mở rộng thì hiện **đủ** văn bản, cuộn trong
 * khung cao tối đa [ExpandedDescriptionMaxHeight].
 */
@Composable
internal fun PlayerInfo(
    description: String,
    expanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (description.isBlank()) return
    val media = AppTheme.colors.media
    var truncated by remember(description) { mutableStateOf(false) }
    Column(modifier = modifier) {
        Text(
            text = description,
            style = shadowed(AppTypography.label.copy(fontWeight = FontWeight.Normal, lineHeight = 19.5.sp)),
            color = media.textBody,
            maxLines = if (expanded) Int.MAX_VALUE else COLLAPSED_LINES,
            overflow = if (expanded) TextOverflow.Clip else TextOverflow.Ellipsis,
            onTextLayout = { if (!expanded) truncated = it.hasVisualOverflow },
            modifier = Modifier
                .applyIf(expanded) { heightIn(max = ExpandedDescriptionMaxHeight).verticalScroll(rememberScrollState()) }
                .tapToToggle(onToggle),
        )
        if (truncated || expanded) {
            Text(
                text = stringResource(if (expanded) R.string.video_less else R.string.video_more),
                style = AppTypography.label,
                color = media.textFaint,
                modifier = Modifier.tapToToggle(onToggle),
            )
        }
    }
}

private const val COLLAPSED_LINES = 2
private val ExpandedDescriptionMaxHeight = 220.dp

private fun Modifier.tapToToggle(onClick: () -> Unit): Modifier =
    pointerInput(onClick) { detectTapGestures { onClick() } }

/**
 * Thanh tiến độ kéo được + thời gian. [dimmed] khi buffering: phần đã chạy thành trắng mờ để không gợi ý là
 * đang phát. Kéo thì chỉ seek lúc thả tay (kéo liên tục mà seek mỗi frame thì player rebuffer liên tục).
 */
@Composable
internal fun PlayerTrack(
    /** Lambda: vị trí đổi mỗi nhịp tick, chỉ thanh này được recompose theo nó (không kéo cả màn). */
    positionMs: () -> Long,
    durationMs: Long,
    dimmed: Boolean,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier,
    /** false = track tĩnh của trang lân cận. */
    seekable: Boolean = true,
) {
    val media = AppTheme.colors.media
    val canSeek = seekable && durationMs > 0
    var dragFraction by remember { mutableStateOf<Float?>(null) }
    val position = positionMs()
    val fraction = dragFraction
        ?: if (durationMs > 0) (position.toFloat() / durationMs).coerceIn(0f, 1f) else 0f
    val seekDescription = stringResource(R.string.player_seek_cd)

    Column(modifier = modifier) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
                .semantics {
                    contentDescription = seekDescription
                    progressBarRangeInfo = ProgressBarRangeInfo(fraction, 0f..1f)
                    if (canSeek) {
                        setProgress { target ->
                            onSeek((target.coerceIn(0f, 1f) * durationMs).toLong())
                            true
                        }
                    }
                }
                .pointerInput(durationMs, canSeek) {
                    if (!canSeek) return@pointerInput
                    detectTapGestures { offset -> onSeek((offset.x / size.width * durationMs).toLong()) }
                }
                .pointerInput(durationMs, canSeek) {
                    if (!canSeek) return@pointerInput
                    detectHorizontalDragGestures(
                        onDragStart = { offset -> dragFraction = (offset.x / size.width).coerceIn(0f, 1f) },
                        onDragEnd = {
                            dragFraction?.let { onSeek((it * durationMs).toLong()) }
                            dragFraction = null
                        },
                        onDragCancel = { dragFraction = null },
                        onHorizontalDrag = { change, _ ->
                            dragFraction = (change.position.x / size.width).coerceIn(0f, 1f)
                        },
                    )
                },
            contentAlignment = Alignment.CenterStart,
        ) {
            val thumb = 11.dp
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(CircleShape)
                    .background(media.track),
            ) {
                Box(
                    Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(fraction)
                        .clip(CircleShape)
                        .then(
                            if (dimmed) Modifier.background(media.trackDimmed) else Modifier.background(AppTheme.colors.gradientCta)
                        ),
                )
            }
            Box(
                Modifier
                    .offset(x = maxWidth * fraction - thumb / 2)
                    .size(thumb)
                    .background(media.text, CircleShape),
            )
        }
        Row(Modifier.fillMaxWidth().padding(top = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            val shownPosition = dragFraction?.let { (it * durationMs).toLong() } ?: position
            Text(formatTime(shownPosition), style = AppTypography.timecode, color = media.textMuted)
            Text(formatTime(durationMs), style = AppTypography.timecode, color = media.textMuted)
        }
    }
}

/** Nút play lớn giữa màn khi user pause. */
@Composable
internal fun CenterPlayIcon(icon: ImageVector, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(64.dp)
            .background(AppTheme.colors.media.glass, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = AppTheme.colors.media.text, modifier = Modifier.size(32.dp))
    }
}

/** `0:42`, `1:02:03`. */
internal fun formatTime(ms: Long): String {
    val totalSeconds = ms.coerceAtLeast(0) / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        String.format(Locale.ROOT, "%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format(Locale.ROOT, "%d:%02d", minutes, seconds)
    }
}
