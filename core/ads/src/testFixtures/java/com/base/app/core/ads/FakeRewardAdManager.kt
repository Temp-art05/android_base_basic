package com.base.app.core.ads

import android.app.Activity
import com.base.app.core.ads.reward.RewardAdManager
import com.base.app.core.ads.reward.RewardAdManager.AdType
import kotlinx.coroutines.CompletableDeferred

/** Rewarded giả cho test ViewModel: kết quả load do test đặt, show luôn nhận thưởng. */
class FakeRewardAdManager : RewardAdManager {
    var rewardedAvailable = true

    /** Đặt để `load` treo tới khi test `complete` — giữ trạng thái Loading. */
    var rewardedGate: CompletableDeferred<Unit>? = null

    var rewardedLoads = 0
        private set

    var preloads = 0
        private set

    override fun register() = Unit

    override fun preload(type: AdType) {
        preloads++
    }

    override fun isEnabled(type: AdType) = true

    override fun isReady(type: AdType) = rewardedAvailable

    override suspend fun load(type: AdType): Boolean {
        rewardedLoads++
        rewardedGate?.await()
        return rewardedAvailable
    }

    override fun show(activity: Activity, type: AdType, onResult: (Boolean) -> Unit) = onResult(true)
}
