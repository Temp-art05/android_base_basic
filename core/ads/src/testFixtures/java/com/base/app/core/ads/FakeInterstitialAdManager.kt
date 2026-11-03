package com.base.app.core.ads

import android.app.Activity
import com.base.app.core.ads.interstitial.InterstitialAdManager
import com.base.app.core.ads.interstitial.InterstitialAdManager.AdType

/** Interstitial giả cho test ViewModel: ghi lại preload, show đóng ngay. */
class FakeInterstitialAdManager : InterstitialAdManager {
    val preloaded = mutableListOf<AdType>()

    override fun register(minIntervalMs: Long) = Unit

    override fun isEnabled(type: AdType) = true

    override fun isReady(type: AdType) = true

    override fun preload(type: AdType) {
        preloaded += type
    }

    override fun showIfReady(activity: Activity, type: AdType, onDone: () -> Unit) = onDone()
}
