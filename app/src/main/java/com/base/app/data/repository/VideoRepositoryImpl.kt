package com.base.app.data.repository

import com.base.app.data.local.datasource.VideoFeedCacheLocalDataSource
import com.base.app.data.mapper.toDomain
import com.base.app.data.remote.datasource.VideoRemoteDataSource
import com.base.app.data.remote.error.toDomainException
import com.base.app.data.util.mapError
import com.base.app.data.util.withCatching
import com.base.app.domain.model.Video
import com.base.app.domain.repository.VideoRepository
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * SAMPLE (video) — ba lớp:
 * - **RAM + single-flight**: Reels và Player cùng xin feed thì chỉ một request chạy; bản thành công giữ tới khi
 *   process chết. Lỗi không giữ để Retry gọi lại thật.
 * - **Đĩa**: tải thành công thì ghi [cache]; tải lỗi (mất mạng, 5xx) mà đĩa có bản cũ thì trả bản đó thay vì lỗi.
 *   Bản đĩa **không** giữ vào RAM — lần gọi sau vẫn thử mạng trước.
 * - Video (byte) cache riêng ở `:core:media` (`SimpleCache`), không qua đây.
 */
class VideoRepositoryImpl(
    private val remote: VideoRemoteDataSource,
    private val cache: VideoFeedCacheLocalDataSource,
) : VideoRepository {

    private val mutex = Mutex()
    private var feed: List<Video>? = null

    override suspend fun getFeed(forceRefresh: Boolean): Result<List<Video>> = mutex.withLock {
        feed?.takeUnless { forceRefresh }?.let { return@withLock Result.success(it) }
        withCatching {
            val dto = remote.getFeed()
            // Parser khoan dung trả rỗng khi sai kiểu cả mảng: coi là lỗi, không ghi đè bản tốt trên đĩa.
            dto.toDomain().also { check(it.isNotEmpty()) { "Video feed has no readable item" }; cache.write(dto) }
        }
            .onSuccess { feed = it }
            .recoverFromCache { cache.read()?.toDomain()?.takeIf { it.isNotEmpty() } }
            .mapError { it.toDomainException() }
    }

    /** Lỗi tải mà đĩa có bản cũ → thành công với bản đó; không có → giữ lỗi gốc. */
    private suspend fun <T> Result<T>.recoverFromCache(read: suspend () -> T?): Result<T> {
        val error = exceptionOrNull() ?: return this
        return read()?.let { Result.success(it) } ?: Result.failure(error)
    }
}
