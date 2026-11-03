package com.base.app.data.local.datasource

import com.base.app.data.local.dao.WatchProgressDao
import com.base.app.data.local.entity.WatchProgressEntity

class WatchProgressLocalDataSourceImpl(
    private val watchProgressDao: WatchProgressDao,
) : WatchProgressLocalDataSource {
    override suspend fun get(videoId: String): WatchProgressEntity? = watchProgressDao.get(videoId)

    override suspend fun upsert(entity: WatchProgressEntity) = watchProgressDao.upsert(entity)
}
