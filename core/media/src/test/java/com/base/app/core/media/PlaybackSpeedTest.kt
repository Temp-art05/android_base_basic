package com.base.app.core.media

import org.junit.Assert.assertEquals
import org.junit.Test

class PlaybackSpeedTest {

    @Test
    fun `rail tap cycles 1 to 1_5 to 2 and back to 1`() {
        assertEquals(1.5f, PlaybackSpeed.next(1f))
        assertEquals(2f, PlaybackSpeed.next(1.5f))
        assertEquals(1f, PlaybackSpeed.next(2f))
    }

    @Test
    fun `speeds outside the cycle picked in the sheet go back to 1`() {
        assertEquals(1f, PlaybackSpeed.next(0.5f))
        assertEquals(1f, PlaybackSpeed.next(3f))
    }

    @Test
    fun `sheet offers the five speeds of the mockup`() {
        assertEquals(listOf(0.5f, 1f, 1.5f, 2f, 3f), PlaybackSpeed.options)
    }
}
