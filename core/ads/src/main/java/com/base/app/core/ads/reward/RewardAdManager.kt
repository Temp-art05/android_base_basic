package com.base.app.core.ads.reward

import android.app.Activity
import android.content.Context
import com.base.app.core.ads.ADS_TAG
import com.base.app.core.ads.onceWith
import com.base.app.core.ads.safely
import com.base.app.core.ads.sdk.AdsSdkFacade
import com.base.app.core.ads.policy.AdPlacement
import com.base.app.core.ads.policy.IsAdPlacementEnabledUseCase
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull
import timber.log.Timber

/**
 * Rewarded theo từng vị trí (như `RewardAdManager` của HomeMorph). Chỉ được show từ một nút user bấm (README §2.3).
 * Mọi hàm **không bao giờ ném**; policy Remote Config được kiểm lại trước mỗi lần load/show.
 */
interface RewardAdManager {

    enum class AdType(val placement: AdPlacement, val adUnitId: String) {
        /** Xem ad để mở một tập khoá (sheet ở Detail, trang khoá trong Player). */
        UNLOCK_EPISODE(AdPlacement.RewardedBonus, "ca-app-pub-3940256099942544/5224354917"),
    }

    fun register()

    fun isEnabled(type: AdType): Boolean

    fun isReady(type: AdType): Boolean

    /** Load sẵn từ lúc màn có tập khoá mở, để lúc user bấm "Watch ad" ad thường đã sẵn. Đã có / đang load → lib bỏ qua. */
    fun preload(type: AdType)

    /** Load (dùng lại bản preload nếu có) và chờ tối đa [LOAD_TIMEOUT_MS]. `false` = không có ad để xem (tắt, offline, không fill). */
    suspend fun load(type: AdType): Boolean

    /** [onResult] `true` khi user xem hết và nhận thưởng; chạy đúng một lần. */
    fun show(activity: Activity, type: AdType, onResult: (earned: Boolean) -> Unit)

    companion object {
        /** Như HomeMorph (`loadTimeoutMillis = 10_000`). */
        const val LOAD_TIMEOUT_MS = 10_000L
    }
}

class RewardAdManagerImpl(
    context: Context,
    private val sdk: AdsSdkFacade,
    private val isPlacementEnabled: IsAdPlacementEnabledUseCase,
) : RewardAdManager {

    private val appContext: Context = context.applicationContext ?: context

    override fun register() = RewardAdManager.AdType.entries.forEach { type ->
        safely("register ${type.placement.id}") {
            sdk.registerRewarded(type.placement.id, type.adUnitId, RewardAdManager.LOAD_TIMEOUT_MS)
        }
    }

    override fun isEnabled(type: RewardAdManager.AdType): Boolean = isPlacementEnabled(type.placement)

    override fun isReady(type: RewardAdManager.AdType): Boolean =
        runCatching { sdk.isFullScreenReady(type.placement.id) }.getOrDefault(false)

    override fun preload(type: RewardAdManager.AdType) {
        if (!isEnabled(type) || isReady(type)) return
        safely("preload ${type.placement.id}") { sdk.preloadFullScreen(appContext, type.placement.id, {}, {}) }
    }

    override suspend fun load(type: RewardAdManager.AdType): Boolean {
        if (!isEnabled(type)) return false
        if (isReady(type)) return true
        val failed = AtomicBoolean(false)
        val requested = safely("preload ${type.placement.id}") {
            sdk.preloadFullScreen(appContext, type.placement.id, onLoaded = {}, onFailed = { failed.set(true) })
        }
        if (!requested) return false
        // Poll thay vì chỉ chờ callback: preload của lib no-op (không callback) khi đã có job đang load.
        return try {
            withTimeoutOrNull(RewardAdManager.LOAD_TIMEOUT_MS) {
                while (!failed.get() && !isReady(type)) delay(POLL_MS)
                !failed.get()
            } ?: false
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Timber.tag(ADS_TAG).w(e, "rewarded load failed")
            false
        }
    }

    override fun show(activity: Activity, type: RewardAdManager.AdType, onResult: (Boolean) -> Unit) {
        val result = onceWith(onResult)
        if (!isEnabled(type) || !isReady(type)) {
            result(false)
            return
        }
        if (!safely("show ${type.placement.id}") { sdk.showRewarded(activity, type.placement.id) { earned -> result(earned) } }) {
            result(false)
        }
    }

    private companion object {
        const val POLL_MS = 250L
    }
}
