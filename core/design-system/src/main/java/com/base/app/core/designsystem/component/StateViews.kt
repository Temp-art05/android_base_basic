package com.base.app.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.base.app.core.designsystem.icon.AppIcons
import com.base.app.core.designsystem.theme.AppTheme
import com.base.app.core.designsystem.theme.AppTypography

/*
 * Trạng thái `.blank` của mockup (SCR-0602 empty, SCR-0603 lỗi mạng, empty nhỏ). Nhận String đã resolve
 * chứ không nhận @StringRes: designsystem không sở hữu string nào — màn hình gọi stringResource() rồi truyền vào.
 */

/**
 * Glyph trong vòng `surface` 72dp viền mảnh, tiêu đề 18sp, mô tả text-2, nút gradient (nếu có).
 * Padding `0 40 80` như mockup: khối hơi lệch lên trên tâm. Cuộn được khi chữ dài (de/ta) trên máy thấp.
 */
@Composable
fun BlankState(
    glyph: ImageVector,
    title: String,
    body: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        BlankStateColumn(
            glyph = glyph,
            title = title,
            body = body,
            actionLabel = actionLabel,
            onAction = onAction,
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(start = 40.dp, end = 40.dp, bottom = 80.dp),
        )
    }
}

@Composable
private fun BlankStateColumn(
    glyph: ImageVector,
    title: String,
    body: String,
    actionLabel: String?,
    onAction: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(MaterialTheme.colorScheme.surface, AppTheme.shapes.full)
                .border(1.dp, MaterialTheme.colorScheme.outline, AppTheme.shapes.full),
            contentAlignment = Alignment.Center,
        ) {
            Icon(glyph, contentDescription = null, tint = AppTheme.colors.text3, modifier = Modifier.size(30.dp))
        }
        Text(
            text = title,
            style = AppTypography.blankTitle,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = AppTheme.dimensions.s5),
        )
        Text(
            text = body,
            style = AppTypography.blankBody,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 9.dp),
        )
        if (actionLabel != null && onAction != null) {
            GradientButton(
                text = actionLabel,
                onClick = onAction,
                modifier = Modifier.padding(top = AppTheme.dimensions.s5),
            )
        }
    }
}

/** SCR-0603: wifi gạch + "Try again". Lỗi không phải mạng dùng cùng layout ([offline] = false, glyph khác). */
@Composable
fun ErrorBlankState(
    title: String,
    body: String,
    retryLabel: String,
    onRetry: () -> Unit,
    offline: Boolean,
    modifier: Modifier = Modifier,
) {
    BlankState(
        glyph = if (offline) AppIcons.WifiOff else AppIcons.Alert,
        title = title,
        body = body,
        actionLabel = retryLabel,
        onAction = onRetry,
        modifier = modifier,
    )
}

/**
 * Như [ErrorBlankState] nhưng không chiếm cả màn, không tự cuộn — đặt trong vùng đã cuộn sẵn (vd. một bước
 * onboarding: lồng `verticalScroll` vào nhau sẽ crash).
 */
@Composable
fun InlineErrorState(
    title: String,
    body: String,
    retryLabel: String,
    onRetry: () -> Unit,
    offline: Boolean,
    modifier: Modifier = Modifier,
) {
    BlankStateColumn(
        glyph = if (offline) AppIcons.WifiOff else AppIcons.Alert,
        title = title,
        body = body,
        actionLabel = retryLabel,
        onAction = onRetry,
        modifier = modifier,
    )
}

@PreviewLightDark
@Composable
private fun ErrorBlankStatePreview() {
    AppTheme {
        Box(Modifier.background(MaterialTheme.colorScheme.background)) {
            ErrorBlankState(
                title = "You're offline",
                body = "Check Wi-Fi or mobile data - the river will still be here.",
                retryLabel = "Try again",
                onRetry = {},
                offline = true,
            )
        }
    }
}
