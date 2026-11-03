package com.base.app.domain.usecase.video

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PlaybackQualityPolicyTest {

    @Test
    fun `downgrades only on auto past threshold`() {
        assertTrue(PlaybackQualityPolicy.shouldDowngrade(isAuto = true, bufferingMs = 5_100))
        assertFalse(PlaybackQualityPolicy.shouldDowngrade(isAuto = true, bufferingMs = 4_000))
        assertFalse(PlaybackQualityPolicy.shouldDowngrade(isAuto = false, bufferingMs = 60_000))
    }

    @Test
    fun `target is nearest lower real quality`() {
        assertEquals("480p", PlaybackQualityPolicy.downgradeTarget("720p", listOf("720p", "480p", "240p")))
        assertNull(PlaybackQualityPolicy.downgradeTarget("240p", listOf("720p", "480p", "240p")))
        assertNull(PlaybackQualityPolicy.downgradeTarget("abc", listOf("480p")))
    }
}
