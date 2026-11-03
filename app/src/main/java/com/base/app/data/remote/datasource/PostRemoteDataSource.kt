package com.base.app.data.remote.datasource

import com.base.app.data.remote.dto.PostDto
import com.base.app.data.remote.dto.PostsResponse

/** SAMPLE — chỉ nói chuyện bằng DTO, không bắt exception (repository lo). */
interface PostRemoteDataSource {
    suspend fun getPosts(skip: Int, limit: Int): PostsResponse

    suspend fun getPost(id: Int): PostDto
}
