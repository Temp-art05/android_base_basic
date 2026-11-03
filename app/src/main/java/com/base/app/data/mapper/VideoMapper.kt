package com.base.app.data.mapper

import com.base.app.data.local.entity.WatchProgressEntity
import com.base.app.data.remote.dto.VideoDto
import com.base.app.data.remote.dto.VideoFeedResponse
import com.base.app.domain.model.Video
import com.base.app.domain.model.WatchProgress

/** Bỏ mục thiếu id (không định danh được để lưu tiến độ) và nguồn URL rỗng. */
fun VideoFeedResponse.toDomain(): List<Video> = videos
    .filter { it.id.isNotBlank() }
    .distinctBy { it.id }
    .map { it.toDomain() }

fun VideoDto.toDomain(): Video = Video(
    id = id,
    title = title,
    description = description,
    thumbnailUrl = thumbnailUrl?.takeIf { it.isNotBlank() },
    sources = sources.filter { (quality, url) -> quality.isNotBlank() && url.isNotBlank() },
)

fun WatchProgressEntity.toDomain(): WatchProgress = WatchProgress(
    videoId = videoId,
    positionMs = positionMs,
    durationMs = durationMs,
    updatedAt = updatedAt,
)

fun WatchProgress.toEntity(): WatchProgressEntity = WatchProgressEntity(
    videoId = videoId,
    positionMs = positionMs,
    durationMs = durationMs,
    updatedAt = updatedAt,
)
