package com.base.app.domain.repository

import com.base.app.domain.model.Video

interface VideoRepository {
    /**
     * Feed đã tải trong phiên thì trả ngay (không gọi mạng lại); [forceRefresh] bỏ qua bản trong RAM.
     * Mạng lỗi mà máy còn bản lần trước thì trả bản đó.
     */
    suspend fun getFeed(forceRefresh: Boolean = false): Result<List<Video>>
}
