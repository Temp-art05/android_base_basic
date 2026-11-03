package com.base.app.core.analytics

import com.base.app.core.analytics.crash.CrashReporter
import com.base.app.core.analytics.crash.FirebaseCrashReporter
import com.base.app.core.analytics.crash.NoOpCrashReporter
import com.base.app.core.analytics.plugin.ConsoleAnalyticsPlugin
import com.base.app.core.analytics.plugin.ExtraAnalyticsPlugins
import com.base.app.core.analytics.plugin.FirebaseAnalyticsPlugin
import com.base.app.core.analytics.util.isFirebaseAvailable
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * @param isDebug bật ConsoleAnalyticsPlugin và tắt gửi dữ liệu thật lên Firebase.
 *
 * Nếu Firebase chưa được cấu hình (chưa có google-services.json) thì module này tự rơi về
 * no-op — base vẫn chạy bình thường, không cần comment/uncomment code như trước.
 */
fun analyticsModule(isDebug: Boolean): Module = module {

    single<CrashReporter> {
        if (isFirebaseAvailable(androidContext())) {
            FirebaseCrashlytics.getInstance()
                .apply { isCrashlyticsCollectionEnabled = !isDebug }
                .let(::FirebaseCrashReporter)
        } else {
            NoOpCrashReporter
        }
    }

    single<Analytics> {
        val plugins = buildList {
            if (isDebug) add(ConsoleAnalyticsPlugin())
            if (isFirebaseAvailable(androidContext())) {
                val firebaseAnalytics = FirebaseAnalytics.getInstance(androidContext())
                firebaseAnalytics.setAnalyticsCollectionEnabled(!isDebug)
                add(FirebaseAnalyticsPlugin(firebaseAnalytics))
            }
            getOrNull<ExtraAnalyticsPlugins>()?.let { addAll(it.plugins) }
        }
        if (plugins.isEmpty()) NoOpAnalytics else AnalyticsImpl(plugins)
    }
}
