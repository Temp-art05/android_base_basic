package com.base.app.data.local.datasource

import com.base.app.data.remote.dto.VideoFeedResponse

/**
 * Bản feed lần tải thành công gần nhất, để mở app khi mất mạng vẫn có dữ liệu. Đọc lỗi (chưa có file,
 * file hỏng) trả `null`; ghi lỗi chỉ log — cache là phần phụ, không làm hỏng lượt tải mạng đã thành công.
 */
interface VideoFeedCacheLocalDataSource {
    suspend fun read(): VideoFeedResponse?

    suspend fun write(feed: VideoFeedResponse)
}
