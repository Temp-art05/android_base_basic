package com.base.app.domain.repository

import com.base.app.domain.model.WatchProgress

interface WatchProgressRepository {
    suspend fun get(videoId: String): WatchProgress?

    suspend fun save(progress: WatchProgress): Result<Unit>
}
