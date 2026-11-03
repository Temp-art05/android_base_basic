package com.base.app.core.analytics.plugin

import android.os.Bundle
import com.base.app.core.analytics.Analytics
import com.base.app.core.analytics.AnalyticsEvent
import com.google.firebase.analytics.FirebaseAnalytics

/**
 * Firebase Analytics plugin implementation
 * Sends events to Firebase Analytics backend
 */
class FirebaseAnalyticsPlugin(
    private val firebaseAnalytics: FirebaseAnalytics
) : AnalyticsPlugin {

    override fun execute(event: AnalyticsEvent) {
        val bundle = when (event) {
            is AnalyticsEvent.Event -> Bundle().apply {
                event.params
                    .filterValues { it != null }
                    .forEach { (key, value) -> putFirebaseValue(key, value) }
            }
            is AnalyticsEvent.OnlyEvent -> null
        }

        firebaseAnalytics.logEvent(event.eventName, bundle)
    }

    override fun setUserProperty(name: String, value: String) {
        firebaseAnalytics.setUserProperty(name, value)
    }

    /** Firebase Analytics chỉ nhận String, Long, Double, Bundle. */
    private fun Bundle.putFirebaseValue(key: String, value: Any?) {
        when (value) {
            null -> Unit
            is String -> putString(key, value)
            is Int -> putLong(key, value.toLong())
            is Long -> putLong(key, value)
            is Float -> putDouble(key, value.toDouble())
            is Double -> putDouble(key, value)
            is Boolean -> putLong(key, if (value) 1L else 0L)
            is Bundle -> putBundle(key, value)
            else -> putString(key, value.toString())
        }
    }
}
