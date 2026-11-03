package com.base.app.core.ads

import com.base.app.core.ads.policy.AdPlacement
import com.base.app.core.ads.policy.AdsConfig
import com.base.app.core.ads.policy.PlacementConfig
import org.junit.Assert.assertEquals
import org.junit.Test

class RemoteTogglesTest {

    private val allIds = AdPlacement.entries.map { it.id }.toSet()

    @Test
    fun `one toggle per placement keyed by placement id`() {
        val toggles = remoteToggles(AdsConfig(), "1.2.0")
        assertEquals(allIds, toggles.keys)
        assertEquals(setOf(true), toggles.values.toSet())
    }

    @Test
    fun `kill switch turns every toggle off`() {
        assertEquals(allIds.associateWith { false }, remoteToggles(AdsConfig(enable = false), "1.2.0"))
    }

    @Test
    fun `disabled version turns every toggle off`() {
        val config = AdsConfig(disabledVersions = listOf("1.2.0"))
        assertEquals(allIds.associateWith { false }, remoteToggles(config, "1.2.0"))
    }

    @Test
    fun `placement switch only affects its own toggle`() {
        val config = AdsConfig(placements = mapOf(AdPlacement.RewardedBonus.id to PlacementConfig(false)))
        val toggles = remoteToggles(config, "1.2.0")
        assertEquals(false, toggles[AdPlacement.RewardedBonus.id])
        assertEquals(true, toggles[AdPlacement.NativeHome.id])
        assertEquals(true, toggles[AdPlacement.InterstitialOnboarding.id])
    }
}
