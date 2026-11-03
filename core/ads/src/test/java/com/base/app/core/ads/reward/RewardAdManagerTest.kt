package com.base.app.core.ads.reward

import android.app.Activity
import android.content.ContextWrapper
import com.base.app.core.ads.FakeAdsSdk
import com.base.app.core.ads.reward.RewardAdManager.AdType
import com.base.app.core.utils.config.AppConfig
import com.base.app.core.ads.FakeAdsConfigRepository
import com.base.app.core.ads.policy.AdsConfig
import com.base.app.core.ads.policy.IsAdPlacementEnabledUseCase
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RewardAdManagerTest {

    private val appConfig = AppConfig("", isDebug = true, isDevFlavor = true, versionCode = 1, versionName = "1.0.0")
    private val repo = FakeAdsConfigRepository()
    private val sdk = FakeAdsSdk()
    private val manager = RewardAdManagerImpl(ContextWrapper(null), sdk, IsAdPlacementEnabledUseCase(repo, appConfig))
    private val activity = Activity()
    private val name = AdType.UNLOCK_EPISODE.placement.id

    @Test
    fun `register uses the google test unit and a 10 second load timeout like HomeMorph`() {
        manager.register()
        assertEquals(listOf(Triple(name, "ca-app-pub-3940256099942544/5224354917", 10_000L)), sdk.registered)
    }

    @Test
    fun `preload asks the sdk once, skips when already loaded or switched off`() {
        manager.preload(AdType.UNLOCK_EPISODE)
        assertEquals(listOf(name), sdk.preloads)
        sdk.ready += name
        manager.preload(AdType.UNLOCK_EPISODE)
        sdk.ready -= name
        repo.config.value = AdsConfig(enable = false)
        manager.preload(AdType.UNLOCK_EPISODE)
        assertEquals(listOf(name), sdk.preloads)
    }

    @Test
    fun `loads - true`() = runTest {
        var result: Boolean? = null
        val job = launchLoad { result = it }
        advanceTimeBy(1_000)
        sdk.pendingLoaded!!()
        advanceTimeBy(500)
        job()
        assertEquals(true, result)
    }

    @Test
    fun `load fails - false`() = runTest {
        var result: Boolean? = null
        val job = launchLoad { result = it }
        advanceTimeBy(100)
        sdk.pendingFailed!!()
        advanceTimeBy(500)
        job()
        assertEquals(false, result)
    }

    @Test
    fun `never answers - times out as unavailable`() = runTest {
        var result: Boolean? = null
        val job = launchLoad { result = it }
        advanceTimeBy(RewardAdManager.LOAD_TIMEOUT_MS + 1_000)
        job()
        assertEquals(false, result)
    }

    @Test
    fun `already loaded - true without a new request`() = runTest {
        sdk.ready += name
        assertTrue(manager.load(AdType.UNLOCK_EPISODE))
        assertEquals(emptyList<String>(), sdk.preloads)
    }

    @Test
    fun `placement off - unavailable without touching the sdk`() = runTest {
        repo.config.value = AdsConfig(enable = false)
        assertFalse(manager.load(AdType.UNLOCK_EPISODE))
        assertEquals(emptyList<String>(), sdk.preloads)
    }

    @Test
    fun `result is passed through, and failures count as not earned`() {
        sdk.ready += name
        var earned: Boolean? = null
        manager.show(activity, AdType.UNLOCK_EPISODE) { earned = it }
        assertEquals(true, earned)

        sdk.rewardEarned = false
        manager.show(activity, AdType.UNLOCK_EPISODE) { earned = it }
        assertEquals(false, earned)

        sdk.throwOnShow = true
        earned = null
        manager.show(activity, AdType.UNLOCK_EPISODE) { earned = it }
        assertEquals(false, earned)
    }

    @Test
    fun `not loaded - not earned and no show`() {
        var earned: Boolean? = null
        manager.show(activity, AdType.UNLOCK_EPISODE) { earned = it }
        assertEquals(false, earned)
        assertTrue(sdk.shown.isEmpty())
    }

    /** Chạy load trong scope của test, trả hàm chờ nó xong. */
    private fun TestScope.launchLoad(onResult: (Boolean) -> Unit): () -> Unit {
        val job = backgroundScope.launch { onResult(manager.load(AdType.UNLOCK_EPISODE)) }
        return { testScheduler.advanceUntilIdle(); check(job.isCompleted) }
    }
}
