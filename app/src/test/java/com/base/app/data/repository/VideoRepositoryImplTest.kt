package com.base.app.data.repository

import com.base.app.data.local.datasource.VideoFeedCacheLocalDataSource
import com.base.app.data.remote.datasource.VideoRemoteDataSource
import com.base.app.data.remote.dto.VideoDto
import com.base.app.data.remote.dto.VideoFeedResponse
import com.base.app.core.utils.error.DomainException
import java.io.IOException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class VideoRepositoryImplTest {

    private val feed = VideoFeedResponse(listOf(VideoDto(id = "a", sources = mapOf("720p" to "u"))))

    private class FakeRemote(var response: VideoFeedResponse) : VideoRemoteDataSource {
        var failure: Exception? = null
        var calls = 0

        override suspend fun getFeed(): VideoFeedResponse {
            calls++
            failure?.let { throw it }
            return response
        }
    }

    private class FakeCache(var stored: VideoFeedResponse? = null) : VideoFeedCacheLocalDataSource {
        override suspend fun read(): VideoFeedResponse? = stored

        override suspend fun write(feed: VideoFeedResponse) {
            stored = feed
        }
    }

    @Test
    fun `success is kept in memory and written to disk`() = runTest {
        val remote = FakeRemote(feed)
        val cache = FakeCache()
        val repository = VideoRepositoryImpl(remote, cache)

        repository.getFeed()
        val second = repository.getFeed()

        assertEquals(1, remote.calls)
        assertEquals(listOf("a"), second.getOrThrow().map { it.id })
        assertEquals(feed, cache.stored)
    }

    @Test
    fun `network failure falls back to disk copy`() = runTest {
        val remote = FakeRemote(feed).apply { failure = IOException("offline") }
        val repository = VideoRepositoryImpl(remote, FakeCache(stored = feed))

        assertEquals(listOf("a"), repository.getFeed().getOrThrow().map { it.id })
    }

    @Test
    fun `disk copy is not kept in memory so next call retries network`() = runTest {
        val remote = FakeRemote(feed).apply { failure = IOException("offline") }
        val repository = VideoRepositoryImpl(remote, FakeCache(stored = feed))

        repository.getFeed()
        repository.getFeed()

        assertEquals(2, remote.calls)
    }

    @Test
    fun `failure without disk copy maps to domain error`() = runTest {
        val remote = FakeRemote(feed).apply { failure = IOException("offline") }
        val result = VideoRepositoryImpl(remote, FakeCache()).getFeed()

        assertTrue(result.exceptionOrNull() is DomainException)
    }

    @Test
    fun `empty feed does not overwrite good disk copy`() = runTest {
        val cache = FakeCache(stored = feed)
        val repository = VideoRepositoryImpl(FakeRemote(VideoFeedResponse()), cache)

        val result = repository.getFeed()

        assertEquals(feed, cache.stored)
        assertEquals(listOf("a"), result.getOrThrow().map { it.id })
    }
}
