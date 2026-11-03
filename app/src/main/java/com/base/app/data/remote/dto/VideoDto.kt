package com.base.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** SAMPLE (video) — `sample-api/videos.json`. */
@Serializable
data class VideoFeedResponse(
    @SerialName("videos") val videos: List<VideoDto> = emptyList(),
)

@Serializable
data class VideoDto(
    @SerialName("id") val id: String = "",
    @SerialName("title") val title: String = "",
    @SerialName("description") val description: String = "",
    @SerialName("thumbnailUrl") val thumbnailUrl: String? = null,
    /** quality → URL. */
    @SerialName("sources") val sources: Map<String, String> = emptyMap(),
)
