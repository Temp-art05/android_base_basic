package com.base.app.core.analytics.plugin

import android.util.Log
import com.base.app.core.analytics.Analytics
import com.base.app.core.analytics.AnalyticsEvent

/**
 * Console Analytics plugin for debugging
 * Logs all analytics events to Logcat for development
 */
class ConsoleAnalyticsPlugin : AnalyticsPlugin {

    override fun execute(event: AnalyticsEvent) {
        when (event) {
            is AnalyticsEvent.OnlyEvent -> {
                Log.d(TAG, "📊 Analytics Event: ${event.eventName}")
            }
            is AnalyticsEvent.Event -> {
                val paramsStr = event.params
                    .filterValues { it != null }
                    .map { (key, value) -> "$key=$value" }
                    .joinToString(", ")
                Log.d(TAG, "📊 Analytics Event: ${event.eventName} | Params: {$paramsStr}")
            }
        }
    }

    override fun setUserProperty(name: String, value: String) {
        Log.d(TAG, "📊 User property: $name=$value")
    }

    companion object {
        private const val TAG = "ConsoleAnalytics"
    }
}
