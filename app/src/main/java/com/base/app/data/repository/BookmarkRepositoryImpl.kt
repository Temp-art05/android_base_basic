package com.base.app.data.repository

import com.base.app.data.local.datasource.BookmarkLocalDataSource
import com.base.app.data.mapper.toBookmarkEntity
import com.base.app.data.mapper.toDomain
import com.base.app.data.util.withLocalCatching
import com.base.app.domain.model.Post
import com.base.app.domain.repository.BookmarkRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** [now] để test cố định thời điểm bookmark. */
class BookmarkRepositoryImpl(
    private val bookmarkLocalDataSource: BookmarkLocalDataSource,
    private val now: () -> Long = System::currentTimeMillis,
) : BookmarkRepository {

    override fun observeBookmarkedIds(): Flow<Set<Int>> =
        bookmarkLocalDataSource.observeIds().map { it.toSet() }

    override fun observeBookmarks(): Flow<List<Post>> =
        bookmarkLocalDataSource.observeAll().map { entities -> entities.map { it.toDomain() } }

    override suspend fun toggleBookmark(post: Post): Result<Unit> = withLocalCatching {
        if (bookmarkLocalDataSource.exists(post.id)) {
            bookmarkLocalDataSource.delete(post.id)
        } else {
            bookmarkLocalDataSource.insert(post.toBookmarkEntity(createdAt = now()))
        }
    }
}
