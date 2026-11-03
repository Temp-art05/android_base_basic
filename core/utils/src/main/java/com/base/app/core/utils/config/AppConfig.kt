package com.base.app.core.utils.config

/**
 * Build-time configuration shared across modules.
 *
 * Flavors/BuildConfig fields are only defined in :app, so :app maps its
 * BuildConfig into this object and binds it as a Koin single at startKoin.
 * Any module can then inject it via get<AppConfig>() without depending on
 * :app's BuildConfig.
 */
data class AppConfig(
    val baseUrl: String,
    val isDebug: Boolean,
    val isDevFlavor: Boolean,
    val versionCode: Int,
    val versionName: String,
)
