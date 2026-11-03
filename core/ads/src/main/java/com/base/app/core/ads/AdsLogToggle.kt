package com.base.app.core.ads

import com.github.dinhn.ads.core.AdsSdk
import com.base.app.core.analytics.remoteconfig.RemoteConfigSource
import com.base.app.core.analytics.remoteconfig.RemoteKeys
import com.base.app.core.utils.config.AppConfig

/**
 * Bật/tắt log ads của lib (Logcat tag `FOR_TEST_ADS`: PRE-LOAD, LOADED, FAILED, SHOWED, PAID…) lúc chạy —
 * Splash cho tap logo 7 lần để gọi. Không lưu bền: mở lại app về giá trị khởi tạo (debug bật, release tắt).
 */
interface AdsLogToggle {
    /** Debug luôn được; release chỉ khi Remote Config `enable_log_ads = true`. */
    fun canToggle(): Boolean

    /** Đảo trạng thái log, trả về trạng thái mới (`true` = đang bật). */
    fun toggle(): Boolean
}

internal class LibAdsLogToggle(
    private val appConfig: AppConfig,
    private val remoteConfig: RemoteConfigSource,
) : AdsLogToggle {
    override fun canToggle(): Boolean =
        (appConfig.isDebug || remoteConfig.get(RemoteKeys.ENABLE_LOG_ADS)) &&
            runCatching { AdsSdk.canToggleLog() }.getOrDefault(false)

    override fun toggle(): Boolean = runCatching { AdsSdk.toggleShowLogAds() }.getOrDefault(false)
}
