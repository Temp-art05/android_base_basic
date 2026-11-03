package com.base.app.data.repository

import com.base.app.data.local.datasource.WatchProgressLocalDataSource
import com.base.app.data.mapper.toDomain
import com.base.app.data.mapper.toEntity
import com.base.app.data.util.withCatching
import com.base.app.data.util.withLocalCatching
import com.base.app.domain.model.WatchProgress
import com.base.app.domain.repository.WatchProgressRepository
import timber.log.Timber

/** SAMPLE (video). */
class WatchProgressRepositoryImpl(
    private val local: WatchProgressLocalDataSource,
) : WatchProgressRepository {

    /** Đọc lỗi → null (xem từ đầu): mất vị trí resume không đáng chặn phát video. */
    override suspend fun get(videoId: String): WatchProgress? =
        withCatching { local.get(videoId)?.toDomain() }
            .onFailure { Timber.w(it, "Read watch progress failed") }
            .getOrNull()

    override suspend fun save(progress: WatchProgress): Result<Unit> = withLocalCatching {
        local.upsert(progress.toEntity())
    }
}
