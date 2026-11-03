package com.base.app.core.analytics

/**
 * Sealed class representing analytics events
 * Supports events with or without parameters
 */
sealed class AnalyticsEvent(open val eventName: String) {
    /**
     * Event without parameters
     * Example: track("screen_view")
     */
    data class OnlyEvent(override val eventName: String) : AnalyticsEvent(eventName)

    /**
     * Event with parameters
     * Example: track("button_click", mapOf("button_name" to "start", "screen" to "home"))
     */
    data class Event(
        override val eventName: String,
        val params: Map<String, Any?>
    ) : AnalyticsEvent(eventName)
}
