package com.base.app.data.util

import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Chạy [action] trên [coroutineContext] (mặc định IO) và gói kết quả vào [Result].
 *
 * Dùng ở repository impl — đó là nơi duy nhất cần đổi thread và chặn exception của
 * Room/Ktor lại. Use case và ViewModel không phải tự làm.
 *
 * ```
 * override suspend fun getProfile(): Result<Profile> = withCatching {
 *     profileRemoteDataSource.getProfile().toDomain()
 * }
 * ```
 *
 * CancellationException được ném tiếp chứ không nuốt thành Result.failure — nếu nuốt thì
 * coroutine bị huỷ sẽ biến thành "lỗi" giả và structured concurrency hỏng.
 */
suspend fun <T> withCatching(
    coroutineContext: CoroutineContext = Dispatchers.IO,
    action: suspend CoroutineScope.() -> T,
): Result<T> = withContext(coroutineContext) {
    runCatching { action() }
        .onFailure { if (it is CancellationException) throw it }
}

/**
 * Đổi exception của [Result] thất bại, giữ nguyên giá trị khi thành công. Dùng sau
 * [withCatching] để dịch lỗi hạ tầng sang lỗi domain.
 */
inline fun <T> Result<T>.mapError(transform: (Throwable) -> Throwable): Result<T> =
    exceptionOrNull()?.let { Result.failure(transform(it)) } ?: this
