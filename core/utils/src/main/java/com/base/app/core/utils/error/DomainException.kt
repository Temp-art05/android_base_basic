package com.base.app.core.utils.error

/**
 * Lỗi mà tầng trên cần phân nhánh. Tầng data dịch lỗi của Ktor/Room sang đây, nên
 * ViewModel chỉ biết domain, không import `:data`.
 *
 * Lỗi nghiệp vụ riêng của project (hết lượt, hết hạn gói...) thì thêm subclass ở đây.
 */
sealed class DomainException(
    message: String? = null,
    cause: Throwable? = null,
) : Exception(message, cause) {

    /** Không có mạng / không tới được server. */
    class Network(cause: Throwable? = null) : DomainException("Network unavailable", cause)

    class Timeout(cause: Throwable? = null) : DomainException("Request timed out", cause)

    /** Phiên đăng nhập hết hạn hoặc không hợp lệ (HTTP 401). */
    class Unauthorized(cause: Throwable? = null) : DomainException("Unauthorized", cause)

    /** Tài nguyên không tồn tại (HTTP 404, show không có file tập). */
    class NotFound(cause: Throwable? = null) : DomainException("Not found", cause)

    /** Server trả lỗi (HTTP 4xx khác 401/404, 5xx). */
    class Server(
        val statusCode: Int,
        message: String? = null,
        cause: Throwable? = null,
    ) : DomainException(message ?: "Server error $statusCode", cause)

    class Unknown(cause: Throwable? = null) : DomainException(cause?.message, cause)
}
