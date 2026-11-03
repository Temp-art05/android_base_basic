package com.base.app.core.ads.policy

import com.base.app.core.utils.config.AppConfig
import com.base.app.core.ads.policy.AdPlacement
import com.base.app.core.ads.policy.AdsConfigRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

class ObserveAdPlacementUseCase(
    private val adsConfigRepository: AdsConfigRepository,
    private val appConfig: AppConfig,
) {
    operator fun invoke(placement: AdPlacement): Flow<Boolean> = adsConfigRepository.config
        .map { AdsPolicy.isEnabled(it, appConfig.versionName, placement) }
        .distinctUntilChanged()
}
