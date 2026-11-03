package com.base.app.core.ui.foundation.error

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.res.stringResource
import com.base.app.core.ui.foundation.R
import com.base.app.core.utils.error.DomainException

/**
 * SCR-0603 chỉ nói "You're offline" khi thật sự mất mạng; lỗi khác cùng layout, chữ khác.
 * [NotFound]: dữ liệu không còn (show bị gỡ) — thử lại vô ích, màn đổi nút Retry thành Back.
 */
enum class ErrorKind { Offline, Generic, NotFound }

/**
 * Lỗi toàn màn trong state: loại (chọn glyph + tiêu đề) + mô tả `@StringRes` — đổi ngôn ngữ thì chữ
 * đổi theo. [messageRes] chỉ hiện với [ErrorKind.Generic]; Offline có câu riêng của design.
 */
@Immutable
data class UiError(val kind: ErrorKind, @param:StringRes val messageRes: Int)

fun Throwable.toUiError(): UiError =
    UiError(kind = if (this is DomainException.Network) ErrorKind.Offline else ErrorKind.Generic, messageRes = toErrorRes())

@Composable
fun UiError.title(): String = stringResource(
    if (kind == ErrorKind.Offline) R.string.state_offline_title else R.string.state_error_title,
)

@Composable
fun UiError.body(): String = stringResource(if (kind == ErrorKind.Offline) R.string.state_offline_body else messageRes)

@Composable
fun retryLabel(): String = stringResource(R.string.state_try_again)
