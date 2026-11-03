package com.base.app.core.ads.policy

import com.base.app.core.ads.policy.AdPlacement
import com.base.app.core.ads.policy.AdsConfig

sealed interface AdsDecision {
    data object Enabled : AdsDecision

    /** [reason] để log và để test đọc rõ nhánh nào chặn. */
    data class Disabled(val reason: Reason) : AdsDecision

    enum class Reason { GlobalOff, VersionBlocked, PlacementOff }
}

/**
 * Placement có được hiện ad không. Thứ tự: `enable=false` → version nằm trong `disabled_versions`
 * → `placements[id].enable=false` → placement không khai thì theo default của code.
 *
 * Kill switch rộng đứng trước cấu hình hẹp: tắt khẩn cấp (toàn app / một bản lỗi) không được để
 * một placement còn ghi `true` vô hiệu hoá. Version so khớp chính xác vì `"1.0"` không phải `"1.0.0"`
 * — chặn nhầm bản đang chạy tốt còn tệ hơn chặn sót (Console sửa lại được trong vài phút).
 */
object AdsPolicy {

    fun decide(config: AdsConfig, versionName: String, placement: AdPlacement): AdsDecision {
        if (!config.enable) return AdsDecision.Disabled(AdsDecision.Reason.GlobalOff)
        if (versionName in config.disabledVersions) return AdsDecision.Disabled(AdsDecision.Reason.VersionBlocked)
        val enabled = config.placements[placement.id]?.enable ?: placement.defaultEnabled
        return if (enabled) AdsDecision.Enabled else AdsDecision.Disabled(AdsDecision.Reason.PlacementOff)
    }

    fun isEnabled(config: AdsConfig, versionName: String, placement: AdPlacement): Boolean =
        decide(config, versionName, placement) == AdsDecision.Enabled
}
