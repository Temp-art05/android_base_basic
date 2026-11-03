package com.base.app.data.remote.datasource

import com.base.app.data.remote.dto.VideoFeedResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

/**
 * SAMPLE (video) — feed là file tĩnh trong repo (`sample-api/videos.json`), tải qua raw GitHub. URL tuyệt đối
 * đè base URL của client. Có API thật: đổi thành path tương đối như `PostRemoteDataSourceImpl`.
 */
class VideoRemoteDataSourceImpl(
    private val httpClient: HttpClient,
) : VideoRemoteDataSource {

    override suspend fun getFeed(): VideoFeedResponse = httpClient.get(FEED_URL).body()

    private companion object {
        const val FEED_URL = "https://raw.githubusercontent.com/Temp-art05/android_base_basic/main/sample-api/videos.json"
    }
}
