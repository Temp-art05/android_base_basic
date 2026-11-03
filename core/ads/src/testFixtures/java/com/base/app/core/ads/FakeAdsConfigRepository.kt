package com.base.app.core.ads

import com.base.app.core.ads.policy.AdsConfig
import com.base.app.core.ads.policy.AdsConfigRepository
import kotlinx.coroutines.flow.MutableStateFlow

class FakeAdsConfigRepository(initial: AdsConfig = AdsConfig.DEFAULT) : AdsConfigRepository {
    override val config = MutableStateFlow(initial)
    override fun current(): AdsConfig = config.value
}
