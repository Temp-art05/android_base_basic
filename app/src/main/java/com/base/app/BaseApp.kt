package com.base.app

import android.app.Application
import android.os.Build
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import com.base.app.core.ads.AdsInitializer
import com.base.app.core.ads.di.adsModule
import com.base.app.core.analytics.Analytics
import com.base.app.core.analytics.analyticsModule
import com.base.app.core.analytics.remoteconfig.RemoteConfigSource
import com.base.app.core.analytics.remoteconfig.remoteConfigModule
import com.base.app.core.designsystem.component.appImageLoader
import com.base.app.core.media.mediaModule
import com.base.app.core.utils.notification.NotificationHelper
import com.base.app.data.di.dataModule
import com.base.app.di.AppScope
import com.base.app.di.appModule
import com.base.app.di.configModule
import com.base.app.feature.main.di.mainModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import timber.log.Timber

class BaseApp : Application(), SingletonImageLoader.Factory {

    override fun newImageLoader(context: PlatformContext): ImageLoader = appImageLoader(context)

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        NotificationHelper.createNotificationChannel(this)

        startKoin {
            androidLogger(if (BuildConfig.DEBUG) Level.INFO else Level.NONE)
            androidContext(this@BaseApp)
            modules(
                configModule,
                appModule,
                // Analytics & Crashlytics tự rơi về no-op nếu chưa có google-services.json
                analyticsModule(isDebug = BuildConfig.IS_DEBUG),
                remoteConfigModule,
                dataModule,
                adsModule,
                mediaModule(),
                // Mỗi feature một module DI
                mainModule,
            )
        }

        get<Analytics>().apply {
            setUserProperty("app_version", BuildConfig.VERSION_NAME)
            setUserProperty("device_model", Build.MODEL)
        }

        // Không có bước này thì remote config không bao giờ được fetch.
        get<CoroutineScope>(AppScope).launch { get<RemoteConfigSource>().fetchAndSync() }
        // MobileAds init chạy nền; request ad đầu tiên chờ remote config áp xong (tối đa 3 s).
        get<AdsInitializer>().start()
    }
}
