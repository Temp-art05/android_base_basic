package com.base.app.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.base.app.data.local.entity.WatchProgressEntity

@Dao
interface WatchProgressDao {
    @Query("SELECT * FROM watch_progress WHERE video_id = :videoId")
    suspend fun get(videoId: String): WatchProgressEntity?

    @Upsert
    suspend fun upsert(entity: WatchProgressEntity)
}
