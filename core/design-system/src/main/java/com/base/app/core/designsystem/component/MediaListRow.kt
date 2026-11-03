package com.base.app.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.base.app.core.designsystem.icon.AppIcons
import com.base.app.core.designsystem.theme.AppTheme
import com.base.app.core.designsystem.theme.AppTypography

/**
 * Hàng `.li` của Library/History/Saved: thumbnail 98×60 bo 8 (+ `.pbar` gradient ở đáy nếu có [progress]),
 * tiêu đề 14sp/600 một dòng, meta 12sp text-3, `.pr` accent dưới meta nếu [showInlineProgress].
 * [trailing] là nút cuối 48dp (⋯, ✕, ›) — tự lo click; null thì không có.
 *
 * Thanh tiến độ fill từ **start** nên RTL chạy từ phải sang.
 */
@Composable
fun MediaListRow(
    imageUrl: String?,
    title: String,
    meta: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    metaColor: Color = AppTheme.colors.text3,
    progress: Float? = null,
    showInlineProgress: Boolean = false,
    trailing: (@Composable () -> Unit)? = null,
) {
    val dimensions = AppTheme.dimensions
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onClick)
            .padding(start = dimensions.s4, end = if (trailing != null) dimensions.s1 else dimensions.s4, top = dimensions.s3, bottom = dimensions.s3),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimensions.s3),
    ) {
        Box(
            Modifier
                .size(dimensions.listThumbWidth, dimensions.listThumbHeight)
                .clip(AppTheme.shapes.sm)
                .background(MaterialTheme.colorScheme.surface),
        ) {
            AppAsyncImage(model = imageUrl, contentDescription = null, modifier = Modifier.matchParentSize())
            if (progress != null) {
                Box(
                    Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .height(3.dp)
                        .background(AppTheme.colors.media.spinnerTrack),
                ) {
                    Box(
                        Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(progress.coerceIn(0f, 1f))
                            .background(AppTheme.colors.gradientCta),
                    )
                }
            }
        }
        Column(Modifier.weight(1f)) {
            Text(
                text = title,
                style = AppTypography.bodySemibold,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = meta,
                style = AppTypography.caption,
                color = metaColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = dimensions.s1),
            )
            if (showInlineProgress && progress != null) {
                Box(
                    Modifier
                        .padding(top = 7.dp)
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(AppTheme.shapes.full)
                        .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                ) {
                    Box(
                        Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(progress.coerceIn(0f, 1f))
                            .background(MaterialTheme.colorScheme.primary),
                    )
                }
            }
        }
        trailing?.invoke()
    }
}

/** Nút cuối hàng 48dp (⋯ / ✕ / ›) — icon text-3. */
@Composable
fun RowEndButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = AppTheme.colors.text3,
) {
    Box(
        modifier = modifier
            .size(AppTheme.dimensions.touchTarget)
            .clip(AppTheme.shapes.full)
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = contentDescription, tint = tint, modifier = Modifier.size(20.dp))
    }
}

/** `›` cuối hàng chỉ để trang trí (cả hàng đã bấm được) — không có vùng chạm riêng. */
@Composable
fun RowChevron(modifier: Modifier = Modifier) {
    Box(modifier.size(AppTheme.dimensions.touchTarget), contentAlignment = Alignment.Center) {
        Icon(AppIcons.ChevronEnd, contentDescription = null, tint = AppTheme.colors.text3, modifier = Modifier.size(18.dp))
    }
}

@PreviewLightDark
@Composable
private fun MediaListRowPreview() {
    AppTheme {
        Column(Modifier.background(MaterialTheme.colorScheme.background)) {
            MediaListRow(
                imageUrl = null,
                title = "The Masked CEO",
                meta = "Ep. 12 / 74 · 4 min left",
                onClick = {},
                progress = 0.62f,
                showInlineProgress = true,
                trailing = { RowEndButton(AppIcons.More, null, onClick = {}) },
            )
            MediaListRow(imageUrl = null, title = "Night of Fate", meta = "Thriller · 52 eps · Complete", onClick = {})
        }
    }
}
