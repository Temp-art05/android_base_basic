package com.base.app.core.ui.foundation.error

import androidx.annotation.StringRes
import com.base.app.core.ui.foundation.R
import com.base.app.core.utils.error.DomainException

/**
 * Lỗi → string res dùng chung. ViewModel giữ `@StringRes Int` trong state, không giữ String,
 * nên đổi ngôn ngữ thì text lỗi đổi theo.
 *
 * ```
 * .onFailure { updateState { copy(isLoading = false, errorRes = it.toErrorRes()) } }
 * ```
 */
@StringRes
fun Throwable.toErrorRes(): Int = when (this) {
    is DomainException.Network -> R.string.error_network
    is DomainException.Timeout -> R.string.error_timeout
    is DomainException.Unauthorized -> R.string.error_unauthorized
    is DomainException.Server -> if (statusCode >= 500) R.string.error_server else R.string.error_unexpected
    else -> R.string.error_unexpected
}
