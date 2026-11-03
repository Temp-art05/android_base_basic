package com.base.app.core.media

import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.cache.Cache
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.SimpleCache
import java.io.File
import org.koin.android.ext.koin.androidContext
import org.koin.core.Koin
import org.koin.dsl.module

/**
 * [tierOverride] để flavor dev ép một mức máy khi kiểm/chụp. `SimpleCache` chỉ được **một**
 * instance mỗi thư mục → `single`, mọi player của pool đọc/ghi qua cùng cache: vuốt ngược lại hay mở
 * lại tập đã lướt qua phát từ đĩa, không tải lại.
 *
 * Cache **theo phiên**: giữ [MediaProfile.maxCachedVideos] video gần nhất, và xoá sạch lúc process
 * khởi động — app bị kill thì không chạy được code dọn, nên dọn ở lần mở kế là cách chắc chắn duy nhất.
 */
@OptIn(UnstableApi::class)
fun mediaModule(tierOverride: () -> DeviceTier? = { null }) = module {
    single { DeviceTierDetector(androidContext(), tierOverride) }
    single { MediaProfile.forTier(get<DeviceTierDetector>().detect()) }
    single<Cache> {
        val context = androidContext()
        val profile = get<MediaProfile>()
        val directory = File(context.cacheDir, "media")
        val databaseProvider = StandaloneDatabaseProvider(context)
        SimpleCache.delete(directory, databaseProvider)
        SimpleCache(directory, RecentVideosEvictor(profile.maxCachedVideos, profile.cacheBytes), databaseProvider)
    }
    single<DataSource.Factory> {
        CacheDataSource.Factory()
            .setCache(get())
            .setUpstreamDataSourceFactory(DefaultDataSource.Factory(androidContext()))
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
    }
    single<EpisodeFeedPlayerFactory> {
        val context = androidContext()
        val profile = get<MediaProfile>()
        val dataSourceFactory = get<DataSource.Factory>()
        EpisodeFeedPlayerFactory { ExoEpisodeFeedPlayer(context, profile, dataSourceFactory) }
    }
}

/**
 * Gọi một lần lúc app khởi động, **trên thread nền**: dựng cache ngay để thư mục của phiên trước bị xoá
 * liền (không đợi tới lúc mở player), và lần mở player đầu không phải chờ khởi tạo cache.
 */
@OptIn(UnstableApi::class)
fun Koin.prepareMediaCache() {
    get<Cache>()
}
