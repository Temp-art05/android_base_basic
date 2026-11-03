package com.base.app.core.ads.policy

import com.base.app.core.ads.policy.AdsConfig
import kotlinx.coroutines.flow.Flow

/** Cấu hình ads từ Remote Config (impl ở `:core:ads`, nơi thấy được Remote Config). */
interface AdsConfigRepository {
    /** Phát lại mỗi khi fetch mới về — fetch muộn sau Splash vẫn có hiệu lực. */
    val config: Flow<AdsConfig>

    /** Giá trị hiện có (fetch phiên này / phiên trước / default) — kiểm ngay trước khi show. */
    fun current(): AdsConfig
}
