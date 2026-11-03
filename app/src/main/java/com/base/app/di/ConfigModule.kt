package com.base.app.di

import com.base.app.BuildConfig
import com.base.app.core.utils.config.AppConfig
import org.koin.dsl.module

/**
 * Maps :app's BuildConfig (flavors live here) into the shared [AppConfig]
 * so other modules can inject build-time values without seeing BuildConfig.
 */
val configModule = module {
    single {
        AppConfig(
            baseUrl = BuildConfig.BASE_URL,
            isDebug = BuildConfig.IS_DEBUG,
            isDevFlavor = BuildConfig.IS_DEV_FLAVOR,
            versionCode = BuildConfig.VERSION_CODE,
            versionName = BuildConfig.VERSION_NAME,
        )
    }
}
