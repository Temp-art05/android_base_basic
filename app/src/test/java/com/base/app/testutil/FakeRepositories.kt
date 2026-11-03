package com.base.app.testutil

import com.base.app.domain.model.Page
import com.base.app.domain.model.Post
import com.base.app.domain.repository.BookmarkRepository
import com.base.app.domain.repository.PostRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

fun post(id: Int) = Post(id = id, title = "Title $id", body = "Body $id")

/** Feed giả [total] post; [failure] khác null thì mọi lần gọi đều lỗi. */
class FakePostRepository(private val total: Int = 50) : PostRepository {
    var failure: Throwable? = null
    val requests = mutableListOf<Pair<Int, Int>>()

    override suspend fun getPosts(skip: Int, limit: Int): Result<Page<Post>> {
        requests += skip to limit
        failure?.let { return Result.failure(it) }
        val end = minOf(skip + limit, total)
        return Result.success(Page(items = (skip until end).map(::post), hasMore = end < total))
    }

    override suspend fun getPost(id: Int): Result<Post> = failure?.let { Result.failure(it) } ?: Result.success(post(id))
}

class FakeBookmarkRepository : BookmarkRepository {
    private val bookmarks = MutableStateFlow<List<Post>>(emptyList())

    override fun observeBookmarkedIds(): Flow<Set<Int>> = bookmarks.map { list -> list.mapTo(HashSet()) { it.id } }

    override fun observeBookmarks(): Flow<List<Post>> = bookmarks

    override suspend fun toggleBookmark(post: Post): Result<Unit> {
        bookmarks.update { list -> if (list.any { it.id == post.id }) list.filterNot { it.id == post.id } else listOf(post) + list }
        return Result.success(Unit)
    }
}
