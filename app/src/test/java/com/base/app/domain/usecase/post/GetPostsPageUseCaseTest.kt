package com.base.app.domain.usecase.post

import com.base.app.domain.model.Page
import com.base.app.domain.model.Post
import com.base.app.domain.repository.PostRepository
import com.base.app.testutil.FakePostRepository
import com.base.app.testutil.post
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class GetPostsPageUseCaseTest {

    @Test
    fun `first page starts at offset 0 with page size`() = runTest {
        val repository = FakePostRepository()
        val page = GetPostsPageUseCase(repository)(loaded = emptyList()).getOrThrow()

        assertEquals(listOf(0 to GetPostsPageUseCase.PAGE_SIZE), repository.requests)
        assertEquals(GetPostsPageUseCase.PAGE_SIZE, page.items.size)
    }

    @Test
    fun `next page skips loaded count and drops duplicates`() = runTest {
        // BE chèn bài mới → trang kế trả lại post 19 đã có.
        val repository = object : PostRepository {
            override suspend fun getPosts(skip: Int, limit: Int) =
                Result.success(Page(items = listOf(post(19), post(20)), hasMore = false))

            override suspend fun getPost(id: Int): Result<Post> = error("unused")
        }
        val loaded = (0 until 20).map(::post)

        val page = GetPostsPageUseCase(repository)(loaded).getOrThrow()

        assertEquals(listOf(post(20)), page.items)
        assertFalse(page.hasMore)
    }
}
