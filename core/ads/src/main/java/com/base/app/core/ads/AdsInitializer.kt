package com.base.app.core.ads

import android.app.Application
import com.github.dinhn.ads.core.AdsSdk
import com.base.app.core.ads.config.AdsConfigParser
import com.base.app.core.ads.interstitial.InterstitialAdManager
import com.base.app.core.ads.nativead.NativeAdManager
import com.base.app.core.ads.reward.RewardAdManager
import com.base.app.core.analytics.remoteconfig.DEFAULT_AWAIT_MS
import com.base.app.core.analytics.remoteconfig.RemoteConfigSource
import com.base.app.core.utils.config.AppConfig
import com.base.app.core.ads.policy.AdPlacement
import com.base.app.core.ads.policy.AdsConfig
import com.base.app.core.ads.policy.AdsConfigRepository
import com.base.app.core.ads.policy.AdsDecision
import com.base.app.core.ads.policy.AdsPolicy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import timber.log.Timber
import com.github.dinhn.ads.core.AdsConfig as LibAdsConfig

/**
 * Khởi động lib ads ở `Application.onCreate` (vai trò `AppAds` của HomeMorph): init (MobileAds khởi tạo nền nhờ
 * meta-data `OPTIMIZE_INITIALIZATION`), log sự kiện, gọi `register()` của từng `*AdManager`, rồi đẩy policy Remote
 * Config vào toggle của lib. Load/show theo vị trí nằm ở [InterstitialAdManager], [RewardAdManager], [NativeAdManager].
 *
 * Lib bật `requireRemoteConfigBeforeRequest`: không request ad nào cho tới khi config lần đầu được áp —
 * tức chờ fetch xong hoặc hết [DEFAULT_AWAIT_MS] (cùng ngưỡng Splash), để placement đã tắt trên Console
 * không kịp hiện trước khi fetch về. Sau đó vẫn quan sát: fetch về muộn cũng áp ngay.
 */
class AdsInitializer(
    private val app: Application,
    private val appConfig: AppConfig,
    private val remoteConfig: RemoteConfigSource,
    private val adsConfigRepository: AdsConfigRepository,
    private val interstitialAdManager: InterstitialAdManager,
    private val rewardAdManager: RewardAdManager,
    private val nativeAdManager: NativeAdManager,
) {
    // Lib dùng HashMap + Compose state, chỉ an toàn trên main thread.
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var interstitialIntervalSec = AdsConfig.DEFAULT_INTERSTITIAL_INTERVAL_SEC

    fun start() {
        val started = runCatching {
            AdsSdk.init(
                app,
                LibAdsConfig(
                    isShowLogAds = appConfig.isDebug,
                    // Tap logo Splash 7 lần (AdsLogToggle) — release còn cần RC `enable_log_ads`.
                    logToggleAllowed = true,
                    remoteToggles = AdPlacement.entries.associate { it.id to it.defaultEnabled },
                    requireRemoteConfigBeforeRequest = true,
                    // Lib tự bắn ad_impression / paid_ad_impression[_tier] / revenue milestone sang Firebase.
                    trackWithFirebase = true,
                    autoGeoTier = true,
                ),
            )
            AdsSdk.setOnAdEvent(AdEventLog::onEvent)
            AdsSdk.setOnPaidEvent(AdEventLog::onRevenue)
            interstitialAdManager.register(interstitialIntervalSec * 1_000)
            rewardAdManager.register()
            nativeAdManager.register()
        }.onFailure { Timber.tag(AdsConfigParser.TAG).e(it, "Ads init failed - running without ads") }
        if (started.isFailure) return

        scope.launch {
            adsConfigRepository.config.collect { config ->
                val toggles = remoteToggles(config, appConfig.versionName)
                Timber.tag(AdsConfigParser.TAG).d("toggles %s (version %s)", toggles, appConfig.versionName)
                runCatching { AdsSdk.setRemoteToggles(toggles) }
                applyInterstitialInterval(config.intervalBetweenInterstitialSec)
            }
        }
        scope.launch {
            withTimeoutOrNull(DEFAULT_AWAIT_MS) { remoteConfig.isReady.first { it } }
            // Toggle đã được collector trên đẩy từ giá trị phiên trước / default trước lúc này.
            runCatching { AdsSdk.setRemoteConfigReady() }
        }
    }

    /**
     * Lib chép min-interval vào placement lúc đăng ký, nên đổi default thôi không đủ (lỗi của HomeMorph:
     * `interval_between_interstitial` không có tác dụng) — đăng ký lại interstitial với giá trị mới.
     */
    private fun applyInterstitialInterval(seconds: Long) {
        if (seconds == interstitialIntervalSec) return
        interstitialIntervalSec = seconds
        runCatching { AdsSdk.setDefaultInterstitialMinIntervalMs(seconds * 1_000) }
        interstitialAdManager.register(seconds * 1_000)
    }
}

/** Toggle đẩy vào lib: key = `placement.id` (khớp key đã đăng ký ở `register`), value = policy D9. */
internal fun remoteToggles(config: AdsConfig, versionName: String): Map<String, Boolean> =
    AdPlacement.entries.associate { placement ->
        placement.id to (AdsPolicy.decide(config, versionName, placement) == AdsDecision.Enabled)
    }
