package com.base.app.data.local.datasource

import com.base.app.data.local.entity.BookmarkEntity
import kotlinx.coroutines.flow.Flow

interface BookmarkLocalDataSource {
    fun observeIds(): Flow<List<Int>>

    fun observeAll(): Flow<List<BookmarkEntity>>

    suspend fun exists(id: Int): Boolean

    suspend fun insert(entity: BookmarkEntity)

    suspend fun delete(id: Int)
}
