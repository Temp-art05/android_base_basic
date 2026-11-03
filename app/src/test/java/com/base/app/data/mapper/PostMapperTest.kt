package com.base.app.data.mapper

import com.base.app.data.remote.dto.PostDto
import com.base.app.data.remote.dto.PostsResponse
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PostMapperTest {

    private fun response(skip: Int, count: Int, total: Int) =
        PostsResponse(posts = List(count) { PostDto(id = skip + it) }, total = total, skip = skip, limit = count)

    @Test
    fun `hasMore is true while skip plus size is below total`() {
        assertTrue(response(skip = 0, count = 20, total = 30).toDomain().hasMore)
    }

    @Test
    fun `hasMore is false on the last page`() {
        assertFalse(response(skip = 20, count = 10, total = 30).toDomain().hasMore)
    }
}
