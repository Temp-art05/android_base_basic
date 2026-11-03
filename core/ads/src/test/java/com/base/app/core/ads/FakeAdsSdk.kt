package com.base.app.core.ads

import android.app.Activity
import android.content.Context
import com.base.app.core.ads.sdk.AdsSdkFacade

/** Lib giả: ghi lại lời gọi; test tự gọi callback để giả lập load xong / lỗi / đóng ad. */
class FakeAdsSdk : AdsSdkFacade {
    val ready = mutableSetOf<String>()
    var throwOnShow = false
    var throwOnPreload = false
    val shown = mutableListOf<String>()
    val preloads = mutableListOf<String>()
    /** Callback lần preload gần nhất — test tự gọi để giả lập load xong/lỗi. */
    var pendingLoaded: (() -> Unit)? = null
    var pendingFailed: (() -> Unit)? = null
    /** Kết quả giả của rewarded khi đóng: earned hay không. */
    var rewardEarned = true
    var dismissInterstitial: (() -> Unit)? = null

    override fun isFullScreenReady(name: String) = name in ready

    override fun preloadFullScreen(context: Context, name: String, onLoaded: () -> Unit, onFailed: () -> Unit) {
        if (throwOnPreload) error("sdk not initialised")
        preloads += name
        pendingLoaded = { ready += name; onLoaded() }
        pendingFailed = onFailed
    }

    override fun showInterstitial(activity: Activity, name: String, onDone: () -> Unit) {
        if (throwOnShow) error("boom")
        shown += name
        dismissInterstitial = onDone
    }

    override fun showRewarded(activity: Activity, name: String, onResult: (Boolean) -> Unit) {
        if (throwOnShow) error("boom")
        shown += name
        onResult(rewardEarned)
    }

    override fun requestNative(context: Context, name: String) = Unit

    /** Mỗi lần đăng ký: name → (ad unit, min interval / load timeout). */
    val registered = mutableListOf<Triple<String, String, Long>>()
    var throwOnRegister = false

    /** minLoadingMs của lần đăng ký interstitial gần nhất. */
    var interstitialLoadingMs = -1L

    override fun registerInterstitial(name: String, adUnitId: String, minLoadingMs: Long, minIntervalMs: Long, maxPerSession: Int) {
        if (throwOnRegister) error("sdk not initialised")
        interstitialLoadingMs = minLoadingMs
        registered += Triple(name, adUnitId, minIntervalMs)
    }

    override fun registerRewarded(name: String, adUnitId: String, loadTimeoutMs: Long) {
        if (throwOnRegister) error("sdk not initialised")
        registered += Triple(name, adUnitId, loadTimeoutMs)
    }

    override fun registerNative(name: String, adUnitId: String) {
        if (throwOnRegister) error("sdk not initialised")
        registered += Triple(name, adUnitId, 0L)
    }
}
