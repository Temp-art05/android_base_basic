package com.base.app.domain.usecase.video

import com.base.app.domain.model.Video
import com.base.app.domain.repository.VideoRepository

/** Feed cho Reels và Player — bỏ video không có nguồn nào (không có gì để phát). */
class GetVideoFeedUseCase(
    private val videoRepository: VideoRepository,
) {
    suspend operator fun invoke(forceRefresh: Boolean = false): Result<List<Video>> =
        videoRepository.getFeed(forceRefresh).map { videos -> videos.filter { it.sources.isNotEmpty() } }
}
