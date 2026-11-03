package com.base.app.core.ads.policy

/**
 * Tham số Remote Config `ads_config` sau khi parse. Default = bản nhúng trong app, dùng khi chưa
 * từng fetch được (offline lần đầu) nên phải chạy được: ads bật, mọi placement theo default của code.
 */
data class AdsConfig(
    val enable: Boolean = true,
    /** So khớp chuỗi chính xác với `versionName` — không trim, phân biệt hoa thường. */
    val disabledVersions: List<String> = emptyList(),
    /** Khoá = [AdPlacement.id]. Placement không khai → [AdPlacement.defaultEnabled]. */
    val placements: Map<String, PlacementConfig> = emptyMap(),
    /** Khoảng cách tối thiểu giữa hai lần hiện interstitial cùng placement (giây). */
    val intervalBetweenInterstitialSec: Long = DEFAULT_INTERSTITIAL_INTERVAL_SEC,
) {
    companion object {
        /** Như HomeMorph: 20 s. */
        const val DEFAULT_INTERSTITIAL_INTERVAL_SEC = 20L

        /** Giống JSON mẫu của README để Console và code cùng một điểm xuất phát. */
        val DEFAULT = AdsConfig(
            enable = true,
            disabledVersions = emptyList(),
            placements = AdPlacement.entries.associate { it.id to PlacementConfig(enable = it.defaultEnabled) },
        )
    }
}

data class PlacementConfig(val enable: Boolean = true)

/** Các vị trí đặt ad. [id] là khoá trong `placements` của Remote Config — đổi là Console hết khớp. */
enum class AdPlacement(val id: String, val defaultEnabled: Boolean) {
    InterstitialOnboarding("interstitial_onboarding", defaultEnabled = true),
    NativeHome("native_home", defaultEnabled = true),
    RewardedBonus("rewarded_bonus", defaultEnabled = true),
}
