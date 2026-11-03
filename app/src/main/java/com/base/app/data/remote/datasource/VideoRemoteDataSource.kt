package com.base.app.data.remote.datasource

import com.base.app.data.remote.dto.VideoFeedResponse

interface VideoRemoteDataSource {
    suspend fun getFeed(): VideoFeedResponse
}
