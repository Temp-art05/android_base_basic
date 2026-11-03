package com.base.app.data.remote.datasource

import com.base.app.data.remote.dto.PostDto
import com.base.app.data.remote.dto.PostsResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

/** Path tương đối (`"posts"`, không `"/posts"`) — base URL đã có `/` cuối. */
class PostRemoteDataSourceImpl(
    private val httpClient: HttpClient,
) : PostRemoteDataSource {

    override suspend fun getPosts(skip: Int, limit: Int): PostsResponse =
        httpClient.get("posts") {
            parameter("skip", skip)
            parameter("limit", limit)
        }.body()

    override suspend fun getPost(id: Int): PostDto = httpClient.get("posts/$id").body()
}
