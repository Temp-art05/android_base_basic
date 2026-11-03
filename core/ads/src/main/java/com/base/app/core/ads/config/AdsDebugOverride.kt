package com.base.app.core.ads.config

import com.base.app.core.ads.policy.AdsConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Chỉ flavor dev: `DebugScreenLauncher` đặt `ads_config` từ extra `debug_ads_config` để chụp màn và
 * thử kill switch mà không cần sửa Console. `null` = dùng Remote Config.
 */
class AdsDebugOverride {
    private val _config = MutableStateFlow<AdsConfig?>(null)
    val config: StateFlow<AdsConfig?> = _config.asStateFlow()

    fun set(rawJson: String?) {
        _config.value = rawJson?.let(AdsConfigParser::parseRaw)
    }
}
