package com.base.app.core.analytics.remoteconfig

import android.content.pm.ApplicationInfo
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.remoteconfig.remoteConfig
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/**
 * Bind [RemoteConfigSource]. Chưa có google-services.json thì rơi về no-op thay vì
 * throw lúc Koin resolve Firebase.remoteConfig.
 */
val remoteConfigModule = module {
    single<RemoteConfigSource> {
        val context = androidContext()
        val firebaseReady = runCatching { FirebaseApp.getApps(context).isNotEmpty() }
            .getOrDefault(false)

        if (firebaseReady) {
            val debuggable = context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
            RemoteConfigManager(
                context = context,
                firebaseRemoteConfig = Firebase.remoteConfig,
                minimumFetchIntervalSeconds = if (debuggable) 0L else RemoteConfigManager.DEFAULT_FETCH_INTERVAL_SECONDS,
            )
        } else {
            NoOpRemoteConfigSource
        }
    }
}
