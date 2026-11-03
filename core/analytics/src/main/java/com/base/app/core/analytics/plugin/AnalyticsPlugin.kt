package com.base.app.core.analytics.plugin

import com.base.app.core.analytics.AnalyticsEvent

/** Một backend analytics (Firebase, console, Adjust...). */
interface AnalyticsPlugin {
    fun execute(event: AnalyticsEvent)

    fun setUserProperty(name: String, value: String) = Unit
}

/**
 * Plugin bổ sung do `:app` khai báo (SDK attribution, ads...) — `:core:analytics` không phải
 * phụ thuộc SDK đó.
 *
 * ```
 * single { ExtraAnalyticsPlugins(listOf(AdjustAnalyticsPlugin())) }
 * ```
 */
class ExtraAnalyticsPlugins(val plugins: List<AnalyticsPlugin>)
