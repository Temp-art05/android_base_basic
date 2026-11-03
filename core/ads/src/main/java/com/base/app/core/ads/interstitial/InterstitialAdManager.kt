package com.base.app.core.ads.interstitial

import android.app.Activity
import android.content.Context
import com.base.app.core.ads.once
import com.base.app.core.ads.safely
import com.base.app.core.ads.sdk.AdsSdkFacade
import com.base.app.core.ads.policy.AdPlacement
import com.base.app.core.ads.policy.AdsConfig
import com.base.app.core.ads.policy.IsAdPlacementEnabledUseCase

/**
 * Interstitial theo từng vị trí (như `InterstitialAdManager` của HomeMorph). Mọi hàm **không bao giờ ném**; policy
 * Remote Config được kiểm lại ngay trước mỗi lần load/show.
 */
interface InterstitialAdManager {

    /** Vị trí đặt interstitial; [adUnitId] là test id chính thức của Google (README §2.3). */
    enum class AdType(val placement: AdPlacement, val adUnitId: String, val maxPerSession: Int) {
        /** Hết onboarding, trước Home — ranh giới luồng tự nhiên, user chưa xem dở gì. */
        ONBOARDING(AdPlacement.InterstitialOnboarding, "ca-app-pub-3940256099942544/1033173712", maxPerSession = 1),
    }

    /** Đăng ký mọi [AdType] với lib. Lib chép min interval lúc đăng ký nên đổi interval phải gọi lại. */
    fun register(minIntervalMs: Long = AdsConfig.DEFAULT_INTERSTITIAL_INTERVAL_SEC * 1_000)

    fun isEnabled(type: AdType): Boolean

    fun isReady(type: AdType): Boolean

    /** Load sẵn để lúc cần hiện ngay, không bắt user chờ. */
    fun preload(type: AdType)

    /** Chỉ hiện khi đã load xong; chưa sẵn / tắt / lỗi → [onDone] ngay. [onDone] luôn chạy đúng một lần. */
    fun showIfReady(activity: Activity, type: AdType, onDone: () -> Unit)

    companion object {
        /** Overlay "loading" trước khi interstitial lên: đủ để mắt thấy chuyển cảnh, không chớp một khung. */
        const val LOADING_MS = 700L
    }
}

class InterstitialAdManagerImpl(
    context: Context,
    private val sdk: AdsSdkFacade,
    private val isPlacementEnabled: IsAdPlacementEnabledUseCase,
) : InterstitialAdManager {

    private val appContext: Context = context.applicationContext ?: context

    override fun register(minIntervalMs: Long) = InterstitialAdManager.AdType.entries.forEach { type ->
        safely("register ${type.placement.id}") {
            sdk.registerInterstitial(type.placement.id, type.adUnitId, InterstitialAdManager.LOADING_MS, minIntervalMs, type.maxPerSession)
        }
    }

    override fun isEnabled(type: InterstitialAdManager.AdType): Boolean = isPlacementEnabled(type.placement)

    override fun isReady(type: InterstitialAdManager.AdType): Boolean =
        runCatching { sdk.isFullScreenReady(type.placement.id) }.getOrDefault(false)

    override fun preload(type: InterstitialAdManager.AdType) {
        if (!isEnabled(type)) return
        safely("preload ${type.placement.id}") { sdk.preloadFullScreen(appContext, type.placement.id, {}, {}) }
    }

    override fun showIfReady(activity: Activity, type: InterstitialAdManager.AdType, onDone: () -> Unit) {
        val done = once(onDone)
        if (!isEnabled(type) || !isReady(type)) {
            done()
            return
        }
        if (!safely("show ${type.placement.id}") { sdk.showInterstitial(activity, type.placement.id, done) }) done()
    }
}
