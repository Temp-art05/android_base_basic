package com.base.app.domain.model

/** SAMPLE (video) — chỗ user dừng ở một video, để mở lại là xem tiếp. */
data class WatchProgress(
    val videoId: String,
    val positionMs: Long,
    val durationMs: Long,
    val updatedAt: Long,
)
