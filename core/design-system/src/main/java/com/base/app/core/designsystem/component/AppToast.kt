package com.base.app.core.designsystem.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.base.app.core.designsystem.icon.AppIcons
import com.base.app.core.designsystem.theme.AppTheme
import com.base.app.core.designsystem.theme.AppTypography
import kotlinx.coroutines.delay

/** Một lần hiện toast. [id] khác nhau để hai toast cùng nội dung liên tiếp vẫn hiện lại. */
@Immutable
data class ToastMessage(val text: String, val success: Boolean, val id: Long = System.nanoTime())

/**
 * Toast SCR-0605: thẻ `surface-3`, chấm xanh success có tick (không dùng accent — accent dành cho hành
 * động), tự tắt sau [ToastDurationMs]. Màn giữ [ToastState] và đặt host ở đáy.
 */
class ToastState {
    var current by mutableStateOf<ToastMessage?>(null)
        private set

    fun show(text: String, success: Boolean = true) {
        current = ToastMessage(text, success)
    }

    internal fun dismiss(message: ToastMessage) {
        if (current?.id == message.id) current = null
    }
}

@Composable
fun rememberToastState(): ToastState = remember { ToastState() }

const val ToastDurationMs = 2_000L

@Composable
fun AppToastHost(state: ToastState, modifier: Modifier = Modifier) {
    val message = state.current
    LaunchedEffect(message?.id) {
        if (message != null) {
            delay(ToastDurationMs)
            state.dismiss(message)
        }
    }
    // Giữ nội dung cũ trong lúc animation thoát.
    var shown by remember { mutableStateOf(message) }
    if (message != null) shown = message
    AnimatedVisibility(
        visible = message != null,
        modifier = modifier,
        enter = fadeIn() + slideInVertically { it / 2 },
        exit = fadeOut() + slideOutVertically { it / 2 },
    ) {
        shown?.let { AppToast(text = it.text, success = it.success) }
    }
}

@Composable
fun AppToast(text: String, success: Boolean, modifier: Modifier = Modifier) {
    val shape = AppTheme.shapes.md
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = AppTheme.dimensions.s4)
            .shadow(elevation = 12.dp, shape = shape)
            .background(MaterialTheme.colorScheme.surfaceContainerHighest, shape)
            .padding(horizontal = AppTheme.dimensions.s4, vertical = 13.dp)
            .semantics { liveRegion = LiveRegionMode.Polite },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier = Modifier
                .size(19.dp)
                .background(if (success) AppTheme.colors.success else AppTheme.colors.lock, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            // Mockup 0605 chỉ có chấm xanh trơn — thêm tick để toast thành công đọc ra ngay "đã xong".
            if (success) {
                Icon(AppIcons.Check, contentDescription = null, tint = MaterialTheme.colorScheme.background, modifier = Modifier.size(13.dp))
            }
        }
        Text(text = text, style = AppTypography.labelSemibold, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Preview
@Composable
private fun AppToastPreview() {
    AppTheme { AppToast(text = "Saved to your Library", success = true) }
}
