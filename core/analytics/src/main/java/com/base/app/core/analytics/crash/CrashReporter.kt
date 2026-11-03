package com.base.app.core.analytics.crash

/** Ghi nhận lỗi non-fatal và thông tin phụ trợ cho crash report. */
interface CrashReporter {
    fun recordException(throwable: Throwable)
    fun log(message: String)
    fun setUserId(userId: String?)
    fun setCustomKey(key: String, value: String)
}

/** Dùng khi chưa cấu hình Crashlytics. */
object NoOpCrashReporter : CrashReporter {
    override fun recordException(throwable: Throwable) = Unit
    override fun log(message: String) = Unit
    override fun setUserId(userId: String?) = Unit
    override fun setCustomKey(key: String, value: String) = Unit
}
