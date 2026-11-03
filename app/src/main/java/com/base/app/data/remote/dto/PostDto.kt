package com.base.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * SAMPLE — envelope của dummyjson: `GET posts` trả `{ posts, total, skip, limit }`. DTO tả đúng contract
 * của BE, không ép vào envelope chung.
 */
@Serializable
data class PostsResponse(
    @SerialName("posts") val posts: List<PostDto> = emptyList(),
    @SerialName("total") val total: Int = 0,
    @SerialName("skip") val skip: Int = 0,
    @SerialName("limit") val limit: Int = 0,
)

@Serializable
data class PostDto(
    @SerialName("id") val id: Int = 0,
    @SerialName("title") val title: String = "",
    @SerialName("body") val body: String = "",
    @SerialName("tags") val tags: List<String> = emptyList(),
)
