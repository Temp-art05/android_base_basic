package com.base.app.core.ads.di

import com.base.app.core.ads.AdsInitializer
import com.base.app.core.ads.AdsLogToggle
import com.base.app.core.ads.LibAdsLogToggle
import com.base.app.core.ads.config.AdsConfigRepositoryImpl
import com.base.app.core.ads.config.AdsDebugOverride
import com.base.app.core.ads.interstitial.InterstitialAdManager
import com.base.app.core.ads.interstitial.InterstitialAdManagerImpl
import com.base.app.core.ads.nativead.NativeAdManager
import com.base.app.core.ads.nativead.NativeAdManagerImpl
import com.base.app.core.ads.reward.RewardAdManager
import com.base.app.core.ads.reward.RewardAdManagerImpl
import com.base.app.core.ads.sdk.AdsSdkFacade
import com.base.app.core.ads.sdk.LibAdsSdkFacade
import com.base.app.core.ads.policy.AdsConfigRepository
import com.base.app.core.ads.policy.IsAdPlacementEnabledUseCase
import com.base.app.core.ads.policy.ObserveAdPlacementUseCase
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val adsModule = module {
    single { AdsDebugOverride() }
    single<AdsConfigRepository> { AdsConfigRepositoryImpl(get(), get()) }
    // Use case cạnh repository: dataModule không ngầm phụ thuộc adsModule.
    factory { ObserveAdPlacementUseCase(get(), get()) }
    factory { IsAdPlacementEnabledUseCase(get(), get()) }
    single<AdsSdkFacade> { LibAdsSdkFacade() }
    single<InterstitialAdManager> { InterstitialAdManagerImpl(androidContext(), get(), get()) }
    single<RewardAdManager> { RewardAdManagerImpl(androidContext(), get(), get()) }
    single<NativeAdManager> { NativeAdManagerImpl(androidContext(), get(), get(), get()) }
    single<AdsLogToggle> { LibAdsLogToggle(get(), get()) }
    single { AdsInitializer(androidApplication(), get(), get(), get(), get(), get(), get()) }
}
