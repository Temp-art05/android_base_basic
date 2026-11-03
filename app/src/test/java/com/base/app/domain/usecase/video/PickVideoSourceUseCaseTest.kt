package com.base.app.domain.usecase.video

import com.base.app.testutil.video
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PickVideoSourceUseCaseTest {

    private val pick = PickVideoSourceUseCase()
    private val ladder = video("v", "240p" to "u240", "720p" to "u720", "480p" to "u480")

    @Test
    fun `no preference picks highest`() {
        assertEquals("720p", pick(ladder)?.quality)
    }

    @Test
    fun `exact preference wins`() {
        assertEquals("u240", pick(ladder, "240p")?.url)
    }

    @Test
    fun `missing preference falls to highest not above it`() {
        assertEquals("480p", pick(ladder, "600p")?.quality)
        // Trần thấp hơn mọi bản → cao nhất (vẫn phát được còn hơn không).
        assertEquals("720p", pick(ladder, "144p")?.quality)
    }

    @Test
    fun `qualities sorted highest first and blank url ignored`() {
        val v = video("v", "480p" to "a", "1080p" to "", "720p" to "b")
        assertEquals(listOf("720p", "480p"), pick.qualitiesOf(v))
    }

    @Test
    fun `no source returns null`() {
        assertNull(pick(video("v", *emptyArray())))
    }
}
