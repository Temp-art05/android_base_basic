package com.base.app.testutil

import com.base.app.domain.model.Video
import com.base.app.domain.model.WatchProgress
import com.base.app.domain.repository.VideoRepository
import com.base.app.domain.repository.WatchProgressRepository

fun video(id: String, vararg sources: Pair<String, String> = arrayOf("720p" to "https://cdn/$id/720.mp4")) = Video(
    id = id,
    title = "Video $id",
    description = "",
    thumbnailUrl = null,
    sources = sources.toMap(),
)

class FakeVideoRepository(var videos: List<Video> = emptyList()) : VideoRepository {
    var failure: Throwable? = null
    var calls = 0

    override suspend fun getFeed(forceRefresh: Boolean): Result<List<Video>> {
        calls++
        return failure?.let { Result.failure(it) } ?: Result.success(videos)
    }
}

class FakeWatchProgressRepository : WatchProgressRepository {
    val saved = mutableMapOf<String, WatchProgress>()

    override suspend fun get(videoId: String): WatchProgress? = saved[videoId]

    override suspend fun save(progress: WatchProgress): Result<Unit> {
        saved[progress.videoId] = progress
        return Result.success(Unit)
    }
}
