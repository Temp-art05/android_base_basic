package com.base.app.data.util

import com.base.app.core.utils.error.DomainException

/**
 * [withCatching] cho ghi Room/DataStore. Không qua `toDomainException()` vì `IOException` của
 * file local sẽ bị dịch thành [DomainException.Network] — báo "mất mạng" sai cho user.
 */
suspend fun <T> withLocalCatching(action: suspend () -> T): Result<T> =
    withCatching { action() }.mapError { it as? DomainException ?: DomainException.Unknown(it) }
