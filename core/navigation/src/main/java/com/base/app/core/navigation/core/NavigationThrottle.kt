package com.base.app.core.navigation.core

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Chặn điều hướng liên tiếp trong [THROTTLE_MILLIS] — chống double-tap mở hai màn.
 * `applyDestination` đã tự áp; code tự điều hướng thì gọi [shouldNavigate] hoặc [awaitWindow].
 */
object NavigationThrottle {

    const val THROTTLE_MILLIS = 300L

    val lastNavTime = MutableStateFlow(0L)

    /** true nếu được điều hướng (và ghi nhận mốc thời gian), false nếu đang trong cửa sổ chặn. */
    fun shouldNavigate(): Boolean {
        val currentTime = System.currentTimeMillis()
        return if (currentTime - lastNavTime.value > THROTTLE_MILLIS) {
            lastNavTime.value = currentTime
            true
        } else {
            false
        }
    }

    /**
     * Đợi hết cửa sổ chặn. Dùng trước điều hướng không do user bấm (deeplink, phiên hết hạn)
     * để request không bị throttle nuốt mất.
     */
    suspend fun awaitWindow() {
        val remaining = THROTTLE_MILLIS - (System.currentTimeMillis() - lastNavTime.value) + 1
        if (remaining > 0) delay(remaining)
    }

    fun reset() {
        lastNavTime.value = 0L
    }
}
