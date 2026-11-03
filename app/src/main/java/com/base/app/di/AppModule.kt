package com.base.app.di

import com.base.app.core.utils.network.AndroidConnectivityMonitor
import com.base.app.core.utils.network.ConnectivityMonitor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module

/** Scope sống theo process, cho việc chạy nền không gắn với màn hình nào. */
val AppScope = named("AppScope")

val appModule = module {
    single(AppScope) { CoroutineScope(SupervisorJob() + Dispatchers.Default) }
    // Một network callback cho cả process — inject `ConnectivityMonitor` ở chỗ cần biết online/Wi-Fi.
    single<ConnectivityMonitor> { AndroidConnectivityMonitor(androidContext()) }
    single { ThemeHolder(get(), get(AppScope)) }
}
