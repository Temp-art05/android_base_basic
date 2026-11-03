package com.base.app.core.ads.sdk

import android.app.Activity
import android.content.Context
import com.github.dinhn.ads.core.AdsSdk

/** Phần lib ads mà các `*AdManager` dùng — interface để test bằng fake. */
interface AdsSdkFacade {
    /**
     * Một ad unit mỗi placement (không waterfall high floor — bài test chỉ có một test id mỗi format). Toggle key = id
     * placement: key lạ mặc định `true` trong lib, nên tách key sẽ làm placement không tắt được.
     */
    /** [minLoadingMs]: overlay loading của lib hiện ít nhất chừng này trước khi ad lên (không chớp giật). */
    fun registerInterstitial(name: String, adUnitId: String, minLoadingMs: Long, minIntervalMs: Long, maxPerSession: Int)

    fun registerRewarded(name: String, adUnitId: String, loadTimeoutMs: Long)

    fun registerNative(name: String, adUnitId: String)

    fun isFullScreenReady(name: String): Boolean

    fun preloadFullScreen(context: Context, name: String, onLoaded: () -> Unit, onFailed: () -> Unit)

    /** [onDone]: đóng / lỗi show / không có ad. */
    fun showInterstitial(activity: Activity, name: String, onDone: () -> Unit)

    /** [onResult]: `true` khi user nhận thưởng trước lúc đóng. */
    fun showRewarded(activity: Activity, name: String, onResult: (earned: Boolean) -> Unit)

    fun requestNative(context: Context, name: String)
}

internal class LibAdsSdkFacade : AdsSdkFacade {
    override fun registerInterstitial(name: String, adUnitId: String, minLoadingMs: Long, minIntervalMs: Long, maxPerSession: Int) =
        AdsSdk.registerInterstitial(
            name = name, highFloorId = "", normalId = adUnitId, enableHighFloorKey = name, enableNormalKey = name,
            minLoadingMs = minLoadingMs, minIntervalMs = minIntervalMs, maxPerSession = maxPerSession,
        )

    override fun registerRewarded(name: String, adUnitId: String, loadTimeoutMs: Long) =
        AdsSdk.registerRewarded(
            name = name, highFloorId = "", normalId = adUnitId, enableHighFloorKey = name, enableNormalKey = name,
            minLoadingMs = 0L, loadTimeoutMillis = loadTimeoutMs,
        )

    override fun registerNative(name: String, adUnitId: String) =
        AdsSdk.registerNative(name = name, highFloorId = "", normalId = adUnitId, enableHighFloorKey = name, enableNormalKey = name)

    override fun isFullScreenReady(name: String): Boolean = AdsSdk.isFullScreenReady(name)

    override fun preloadFullScreen(context: Context, name: String, onLoaded: () -> Unit, onFailed: () -> Unit) =
        AdsSdk.preloadFullScreen(context, name, onLoaded, onFailed)

    override fun showInterstitial(activity: Activity, name: String, onDone: () -> Unit) = guarded(activity) {
        AdsSdk.showInterstitial(activity, name, onNext = {
            AdTransitionGuard.hide(activity)
            onDone()
        })
    }

    override fun showRewarded(activity: Activity, name: String, onResult: (Boolean) -> Unit) = guarded(activity) {
        AdsSdk.showRewarded(activity, name, onNext = { earned ->
            AdTransitionGuard.hide(activity)
            onResult(earned)
        })
    }

    /** Phủ [AdTransitionGuard] suốt lúc ad lên; lib ném thì gỡ ngay (caller coi như không có ad). */
    private inline fun guarded(activity: Activity, show: () -> Unit) {
        AdTransitionGuard.show(activity)
        try {
            show()
        } catch (e: Exception) {
            AdTransitionGuard.hide(activity)
            throw e
        }
    }

    override fun requestNative(context: Context, name: String) = AdsSdk.preloadNative(context, name)
}
