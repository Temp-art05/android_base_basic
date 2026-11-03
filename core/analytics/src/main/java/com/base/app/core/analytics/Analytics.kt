package com.base.app.core.analytics

import com.base.app.core.analytics.plugin.AnalyticsPlugin
import timber.log.Timber

/**
 * Điểm vào duy nhất để track event.
 *
 * Là interface chứ không phải `object` global: mọi thứ khác trong project đều inject qua
 * Koin, và một object global thì test không mock được.
 */
interface Analytics {
    fun track(eventName: String)
    fun track(eventName: String, params: Map<String, Any?>)
    fun track(eventName: String, vararg params: Pair<String, Any?>)
    fun setUserProperty(name: String, value: String)
}

/**
 * Fan-out event ra mọi plugin đã đăng ký. Một plugin lỗi không được làm hỏng plugin khác
 * và tuyệt đối không được làm crash luồng nghiệp vụ.
 */
class AnalyticsImpl(
    private val plugins: List<AnalyticsPlugin>,
) : Analytics {

    override fun track(eventName: String) = dispatch(AnalyticsEvent.OnlyEvent(eventName))

    override fun track(eventName: String, params: Map<String, Any?>) =
        dispatch(AnalyticsEvent.Event(eventName, params.mapValues { it.value.sanitized() }))

    override fun track(eventName: String, vararg params: Pair<String, Any?>) =
        track(eventName, params.toMap())

    override fun setUserProperty(name: String, value: String) {
        plugins.forEach { plugin ->
            runCatching { plugin.setUserProperty(name, value) }
                .onFailure { Timber.e(it, "Analytics plugin failed: ${plugin::class.simpleName}") }
        }
    }

    private fun dispatch(event: AnalyticsEvent) {
        plugins.forEach { plugin ->
            runCatching { plugin.execute(event) }
                .onFailure { Timber.e(it, "Analytics plugin failed: ${plugin::class.simpleName}") }
        }
    }
}

/** Param null/rỗng thành "unknown" để dashboard không tách ra một nhóm "(not set)" riêng. */
internal fun Any?.sanitized(): Any = when (this) {
    null -> UNKNOWN
    is String -> ifBlank { UNKNOWN }
    is Number, is Boolean -> this
    else -> toString().ifBlank { UNKNOWN }
}

private const val UNKNOWN = "unknown"

/** Dùng khi chưa cấu hình analytics — base phải chạy được mà không cần setup gì. */
object NoOpAnalytics : Analytics {
    override fun track(eventName: String) = Unit
    override fun track(eventName: String, params: Map<String, Any?>) = Unit
    override fun track(eventName: String, vararg params: Pair<String, Any?>) = Unit
    override fun setUserProperty(name: String, value: String) = Unit
}
