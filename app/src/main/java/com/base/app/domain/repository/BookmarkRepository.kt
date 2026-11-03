package com.base.app.domain.repository

import com.base.app.domain.model.Post
import kotlinx.coroutines.flow.Flow

/** SAMPLE — post đã bookmark, lưu offline (Room). */
interface BookmarkRepository {
    fun observeBookmarkedIds(): Flow<Set<Int>>

    /** Mới bookmark trước. */
    fun observeBookmarks(): Flow<List<Post>>

    suspend fun toggleBookmark(post: Post): Result<Unit>
}
