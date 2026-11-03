package com.base.app.feature.main.presentation.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.base.app.core.designsystem.component.ErrorBlankState
import com.base.app.core.ui.foundation.error.ErrorKind
import com.base.app.core.ui.foundation.error.UiError
import com.base.app.core.ui.foundation.error.body
import com.base.app.core.ui.foundation.error.retryLabel
import com.base.app.core.ui.foundation.error.title

/** Trạng thái lỗi toàn màn cho mọi màn dữ liệu — Offline có câu riêng, lỗi khác cùng layout. */
@Composable
internal fun UiErrorState(error: UiError, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    ErrorBlankState(
        title = error.title(),
        body = error.body(),
        retryLabel = retryLabel(),
        onRetry = onRetry,
        offline = error.kind == ErrorKind.Offline,
        modifier = modifier,
    )
}
