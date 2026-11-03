package com.base.app.core.ads.policy

import com.base.app.core.utils.config.AppConfig
import com.base.app.core.ads.policy.AdPlacement
import com.base.app.core.ads.policy.AdsConfigRepository

/** Kiểm một lần, ngay trước khi load/show — cấu hình có thể đã đổi từ lúc màn mở. */
class IsAdPlacementEnabledUseCase(
    private val adsConfigRepository: AdsConfigRepository,
    private val appConfig: AppConfig,
) {
    operator fun invoke(placement: AdPlacement): Boolean =
        AdsPolicy.isEnabled(adsConfigRepository.current(), appConfig.versionName, placement)
}
