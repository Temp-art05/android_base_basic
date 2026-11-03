package com.base.app.data.local.datasource

import com.base.app.data.remote.dto.VideoFeedResponse
import java.io.File
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import timber.log.Timber

/** Lưu nguyên DTO dạng JSON trong [directory] (`filesDir`, không phải `cacheDir`: hệ thống không tự xoá). */
class VideoFeedCacheLocalDataSourceImpl(
    private val directory: File,
    private val json: Json,
) : VideoFeedCacheLocalDataSource {

    override suspend fun read(): VideoFeedResponse? = withContext(Dispatchers.IO) {
        val file = File(directory, FILE_NAME)
        if (!file.exists()) return@withContext null
        try {
            json.decodeFromString(VideoFeedResponse.serializer(), file.readText())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Timber.w(e, "Video feed cache unreadable")
            null
        }
    }

    override suspend fun write(feed: VideoFeedResponse) = withContext(Dispatchers.IO) {
        try {
            directory.mkdirs()
            // Ghi file tạm rồi đổi tên: process chết giữa chừng không để lại file cụt.
            val tmp = File(directory, "$FILE_NAME.tmp")
            tmp.writeText(json.encodeToString(VideoFeedResponse.serializer(), feed))
            if (!tmp.renameTo(File(directory, FILE_NAME))) Timber.w("Video feed cache rename failed")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Timber.w(e, "Video feed cache not written")
        }
    }

    private companion object {
        const val FILE_NAME = "video_feed.json"
    }
}
