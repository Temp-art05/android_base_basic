package com.base.app.domain.repository

import com.base.app.domain.model.Page
import com.base.app.domain.model.Post

/**
 * SAMPLE — interface ở `domain`, impl ở `data` (`PostRepositoryImpl`). Hàm một lần trả `Result`,
 * stream trả thẳng `Flow`.
 */
interface PostRepository {
    suspend fun getPosts(skip: Int, limit: Int): Result<Page<Post>>

    suspend fun getPost(id: Int): Result<Post>
}
