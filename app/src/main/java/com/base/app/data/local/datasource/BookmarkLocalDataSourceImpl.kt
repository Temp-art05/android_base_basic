package com.base.app.data.local.datasource

import com.base.app.data.local.dao.BookmarkDao
import com.base.app.data.local.entity.BookmarkEntity
import kotlinx.coroutines.flow.Flow

class BookmarkLocalDataSourceImpl(
    private val bookmarkDao: BookmarkDao,
) : BookmarkLocalDataSource {
    override fun observeIds(): Flow<List<Int>> = bookmarkDao.observeIds()

    override fun observeAll(): Flow<List<BookmarkEntity>> = bookmarkDao.observeAll()

    override suspend fun exists(id: Int): Boolean = bookmarkDao.exists(id)

    override suspend fun insert(entity: BookmarkEntity) = bookmarkDao.insert(entity)

    override suspend fun delete(id: Int) = bookmarkDao.deleteById(id)
}
