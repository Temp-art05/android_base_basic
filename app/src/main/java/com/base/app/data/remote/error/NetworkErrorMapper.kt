package com.base.app.data.remote.error

import com.base.app.core.utils.error.DomainException
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.HttpRequestTimeoutException
import java.io.IOException
import java.net.SocketTimeoutException as JavaSocketTimeoutException
import kotlinx.serialization.SerializationException

/**
 * Dịch lỗi hạ tầng (Ktor/IO/serialization) sang [DomainException]. Gọi ở repository:
 *
 * ```
 * withCatching { remote.getProfile().toDomain() }.mapError { it.toDomainException() }
 * ```
 *
 * Mã lỗi nghiệp vụ của BE (`errorCode`) cần phân nhánh thì map thành subclass riêng ở đây
 * hoặc ở repository, trước nhánh [ApiException] chung.
 */
fun Throwable.toDomainException(): DomainException = when (this) {
    is DomainException -> this
    is ApiException -> when {
        isUnauthorized -> DomainException.Unauthorized(this)
        isNotFound -> DomainException.NotFound(this)
        else -> DomainException.Server(statusCode = status, message = serverMessage, cause = this)
    }
    is HttpRequestTimeoutException,
    is ConnectTimeoutException,
    is SocketTimeoutException,
    is JavaSocketTimeoutException -> DomainException.Timeout(this)
    is IOException -> DomainException.Network(this)
    is SerializationException -> DomainException.Unknown(this)
    else -> DomainException.Unknown(this)
}
