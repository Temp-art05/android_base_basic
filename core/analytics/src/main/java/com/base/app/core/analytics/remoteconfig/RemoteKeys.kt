package com.base.app.core.analytics.remoteconfig

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject

/**
 * Định nghĩa tất cả Remote Config keys.
 *
 * ```
 * val FEATURE_ENABLED = BooleanKey(key = "feature_enabled", default = false)
 * val MAX_RETRY = LongKey(key = "max_retry", default = 3L)
 * val APP_CONFIG = JsonKey(key = "app_config", default = AppConfigRc(), serializer = AppConfigRc.serializer())
 * ```
 * Khai báo xong nhớ thêm vào [ALL_KEYS].
 */
object RemoteKeys {

    /**
     * Bật/tắt ads theo version + placement (README §2.4). Giữ dạng [JsonElement] thô: `:core:ads` tự
     * parse từng field cho khoan dung — một placement gõ sai kiểu trên Console không được làm hỏng
     * cả kill switch. Default nhúng = mọi placement bật, dùng khi offline lần đầu.
     */
    val ADS_CONFIG = JsonKey<JsonElement>(
        key = "ads_config",
        default = defaultAdsConfig(),
        serializer = JsonElement.serializer(),
    )

    /**
     * Cho phép tap logo Splash 7 lần để bật/tắt log ads (`FOR_TEST_ADS`) trên bản release. Bản debug
     * luôn cho phép. Như HomeMorph.
     */
    val ENABLE_LOG_ADS = BooleanKey(key = "enable_log_ads", default = false)

    val ALL_KEYS: List<RemoteConfigKey<*>> = listOf(
        ADS_CONFIG,
        ENABLE_LOG_ADS,
    )

    private val defaultsJson = Json { encodeDefaults = true }

    /** Default của code, seed vào Firebase bằng `setDefaultsAsync`. */
    fun defaultsMap(): Map<String, Any> = ALL_KEYS.associate { key ->
        key.key to when (key) {
            is StringKey -> key.default
            is LongKey -> key.default
            is BooleanKey -> key.default
            is JsonKey<*> -> key.encodeDefault(defaultsJson)
        }
    }

    private fun defaultAdsConfig(): JsonObject = buildJsonObject {
        put("enable", true)
        putJsonArray("disabled_versions") {}
        putJsonObject("placements") {
            listOf("interstitial_onboarding", "native_home", "rewarded_bonus").forEach { id ->
                putJsonObject(id) { put("enable", true) }
            }
        }
        put("interval_between_interstitial", 20)
    }

    private fun <T> JsonKey<T>.encodeDefault(json: Json): String = json.encodeToString(serializer, default)
}
