package com.base.app.core.ads.interstitial

import android.app.Activity
import android.content.ContextWrapper
import com.base.app.core.ads.FakeAdsSdk
import com.base.app.core.ads.interstitial.InterstitialAdManager.AdType
import com.base.app.core.utils.config.AppConfig
import com.base.app.core.ads.FakeAdsConfigRepository
import com.base.app.core.ads.policy.AdsConfig
import com.base.app.core.ads.policy.PlacementConfig
import com.base.app.core.ads.policy.IsAdPlacementEnabledUseCase
import org.junit.Assert.assertEquals
import org.junit.Test

class InterstitialAdManagerTest {

    private val appConfig = AppConfig("", isDebug = true, isDevFlavor = true, versionCode = 1, versionName = "1.0.0")
    private val repo = FakeAdsConfigRepository()
    private val sdk = FakeAdsSdk()
    private val manager = InterstitialAdManagerImpl(ContextWrapper(null), sdk, IsAdPlacementEnabledUseCase(repo, appConfig))
    private val activity = Activity()
    private val name = AdType.ONBOARDING.placement.id

    @Test
    fun `register uses the google test unit, a 700 ms loading overlay and the given min interval, again when it changes`() {
        manager.register(minIntervalMs = 20_000)
        manager.register(minIntervalMs = 45_000)
        assertEquals(
            listOf(
                Triple(name, "ca-app-pub-3940256099942544/1033173712", 20_000L),
                Triple(name, "ca-app-pub-3940256099942544/1033173712", 45_000L),
            ),
            sdk.registered,
        )
        assertEquals(700L, sdk.interstitialLoadingMs)
    }

    @Test
    fun `register swallows a throwing sdk`() {
        sdk.throwOnRegister = true
        manager.register()
    }

    @Test
    fun `placement off - onDone runs once and the sdk is never asked to show`() {
        repo.config.value = AdsConfig(placements = mapOf(name to PlacementConfig(false)))
        sdk.ready += name
        var done = 0
        manager.showIfReady(activity, AdType.ONBOARDING) { done++ }
        assertEquals(1, done)
        assertEquals(emptyList<String>(), sdk.shown)
    }

    @Test
    fun `version blocked - nothing is preloaded`() {
        repo.config.value = AdsConfig(disabledVersions = listOf("1.0.0"))
        manager.preload(AdType.ONBOARDING)
        assertEquals(emptyList<String>(), sdk.preloads)
    }

    @Test
    fun `not ready yet - go on immediately instead of waiting`() {
        var done = 0
        manager.showIfReady(activity, AdType.ONBOARDING) { done++ }
        assertEquals(1, done)
        assertEquals(emptyList<String>(), sdk.shown)
    }

    @Test
    fun `ready - shows and continues once on dismiss even if the sdk calls back twice`() {
        sdk.ready += name
        var done = 0
        manager.showIfReady(activity, AdType.ONBOARDING) { done++ }
        assertEquals(0, done)
        sdk.dismissInterstitial!!()
        sdk.dismissInterstitial!!()
        assertEquals(1, done)
    }

    @Test
    fun `sdk throws on show - onDone still runs exactly once`() {
        sdk.ready += name
        sdk.throwOnShow = true
        var done = 0
        manager.showIfReady(activity, AdType.ONBOARDING) { done++ }
        assertEquals(1, done)
    }

    @Test
    fun `sdk throws on preload - swallowed`() {
        sdk.throwOnPreload = true
        manager.preload(AdType.ONBOARDING)
    }
}
