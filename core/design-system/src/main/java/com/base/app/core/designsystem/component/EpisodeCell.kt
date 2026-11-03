package com.base.app.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.base.app.core.designsystem.ext.applyIf
import com.base.app.core.designsystem.theme.AppTheme
import com.base.app.core.designsystem.theme.AppTypography

enum class EpisodeCellStyle { Unwatched, Watched, Playing, Locked }

/**
 * Ô tập `.ep` — một "ngôn ngữ khoá" cho SCR-0301, 0303 và player: đã xem xám mờ, đang phát
 * gradient, chưa xem chữ sáng, **khoá = ô lõm + số + khoá vàng ở góc**. [onSheet] = ô nằm trong
 * sheet: ô khoá dùng nền `bg` + viền để nổi lên khỏi mặt sheet (ghi chú SCR-0303).
 */
@Composable
fun EpisodeCell(
    label: String,
    style: EpisodeCellStyle,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onSheet: Boolean = false,
    lockedDescription: String? = null,
) {
    val colors = MaterialTheme.colorScheme
    val shape = AppTheme.shapes.sm
    val textColor = when (style) {
        EpisodeCellStyle.Playing -> colors.onPrimary
        EpisodeCellStyle.Watched, EpisodeCellStyle.Locked -> AppTheme.colors.text4
        EpisodeCellStyle.Unwatched -> colors.onSurfaceVariant
    }
    Box(
        modifier = modifier
            .height(AppTheme.dimensions.episodeCell)
            .clip(shape)
            .then(
                when (style) {
                    EpisodeCellStyle.Playing -> Modifier.background(AppTheme.colors.gradientCta, shape)
                    EpisodeCellStyle.Locked -> if (onSheet) {
                        Modifier
                            .background(colors.background, shape)
                            .border(1.dp, colors.outline, shape)
                    } else {
                        Modifier.background(colors.surface, shape)
                    }
                    else -> Modifier.background(colors.surfaceVariant, shape)
                }
            )
            .clickable(role = Role.Button, onClick = onClick)
            .applyIf(style == EpisodeCellStyle.Locked && lockedDescription != null) {
                semantics { contentDescription = "$label, $lockedDescription" }
            },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = AppTypography.body.copy(
                fontWeight = if (style == EpisodeCellStyle.Playing) FontWeight.Bold else FontWeight.SemiBold,
            ),
            color = textColor,
        )
        if (style == EpisodeCellStyle.Locked) {
            Icon(
                imageVector = Icons.Outlined.Lock,
                contentDescription = null,
                tint = AppTheme.colors.lock,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(5.dp)
                    .size(AppTheme.dimensions.episodeLock),
            )
        }
    }
}

@Preview
@Composable
private fun EpisodeCellPreview() {
    AppTheme {
        Row(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(9.dp),
        ) {
            EpisodeCellStyle.entries.forEachIndexed { i, style ->
                EpisodeCell(label = "${i + 4}", style = style, onClick = {}, modifier = Modifier.width(56.dp))
            }
        }
    }
}
