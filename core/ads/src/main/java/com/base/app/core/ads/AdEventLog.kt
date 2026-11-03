package com.base.app.core.ads

import com.github.dinhn.ads.core.AdEvent
import com.github.dinhn.ads.core.AdValue
import timber.log.Timber

/**
 * Log mọi sự kiện lib phát ra (`AdsSdk.setOnAdEvent` / `setOnPaidEvent`) qua Timber tag [TAG] — chỉ bản
 * debug có Timber tree. Firebase do lib tự bắn (`trackWithFirebase`), đây chỉ để nhìn khi phát triển.
 */
internal object AdEventLog {
    const val TAG = "AppAdsEvents"

    fun onEvent(event: AdEvent) {
        val source = if (event.adSource.isNotEmpty()) " src=${event.adSource}" else ""
        val error = if (event.error.isNotEmpty()) " error=${event.error}" else ""
        Timber.tag(TAG).d("[%s] %s · %s%s%s", event.type, event.format, shortId(event.adUnitId), source, error)
    }

    fun onRevenue(value: AdValue) {
        val usd = value.valueMicros / 1_000_000.0
        Timber.tag(TAG).d("[PAID] %s · %.6f %s · net=%s", value.format, usd, value.currencyCode, value.network.ifEmpty { "?" })
    }

    private fun shortId(id: String) = id.substringAfterLast('/')
}
