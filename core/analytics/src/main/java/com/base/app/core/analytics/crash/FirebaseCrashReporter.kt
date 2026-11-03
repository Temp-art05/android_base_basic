package com.base.app.core.analytics.crash

import com.google.firebase.crashlytics.FirebaseCrashlytics

class FirebaseCrashReporter(
    private val crashlytics: FirebaseCrashlytics,
) : CrashReporter {
    override fun recordException(throwable: Throwable) = crashlytics.recordException(throwable)
    override fun log(message: String) = crashlytics.log(message)
    override fun setUserId(userId: String?) = crashlytics.setUserId(userId.orEmpty())
    override fun setCustomKey(key: String, value: String) = crashlytics.setCustomKey(key, value)
}
