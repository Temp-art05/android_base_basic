package com.base.app.domain.usecase.progress

import com.base.app.domain.model.WatchProgress
import com.base.app.testutil.FakeWatchProgressRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetResumePositionUseCaseTest {

    private val repository = FakeWatchProgressRepository()
    private val resume = GetResumePositionUseCase(repository)

    @Test
    fun `never watched starts at zero`() = runTest {
        assertEquals(0L, resume("v"))
    }

    @Test
    fun `resumes where stopped`() = runTest {
        repository.saved["v"] = WatchProgress("v", positionMs = 42_000, durationMs = 100_000, updatedAt = 0)
        assertEquals(42_000L, resume("v"))
    }

    @Test
    fun `almost finished restarts`() = runTest {
        repository.saved["v"] = WatchProgress("v", positionMs = 98_500, durationMs = 100_000, updatedAt = 0)
        assertEquals(0L, resume("v"))
    }
}
