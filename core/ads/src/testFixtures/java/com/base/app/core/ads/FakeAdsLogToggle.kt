package com.base.app.core.ads

/** Toggle log giả cho test Splash: đếm số lần toggle, quyền do test đặt. */
class FakeAdsLogToggle(var allowed: Boolean = true) : AdsLogToggle {
    var enabled = false
        private set
    var toggles = 0
        private set

    override fun canToggle(): Boolean = allowed

    override fun toggle(): Boolean {
        toggles++
        enabled = !enabled
        return enabled
    }
}
