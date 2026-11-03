package com.base.app.data.local.datasource

import com.base.app.data.local.entity.WatchProgressEntity

interface WatchProgressLocalDataSource {
    suspend fun get(videoId: String): WatchProgressEntity?

    suspend fun upsert(entity: WatchProgressEntity)
}
