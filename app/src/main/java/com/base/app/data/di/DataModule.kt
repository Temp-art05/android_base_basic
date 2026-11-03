package com.base.app.data.di

import androidx.room.Room
import com.base.app.core.utils.config.AppConfig
import com.base.app.data.local.database.AppDatabase
import com.base.app.data.local.datasource.BookmarkLocalDataSource
import com.base.app.data.local.datasource.BookmarkLocalDataSourceImpl
import com.base.app.data.local.datasource.UserPreferencesLocalDataSource
import com.base.app.data.local.datasource.UserPreferencesLocalDataSourceImpl
import com.base.app.data.remote.datasource.PostRemoteDataSource
import com.base.app.data.remote.datasource.PostRemoteDataSourceImpl
import com.base.app.data.remote.http.ApiConfig
import com.base.app.data.remote.http.provideHttpClient
import com.base.app.data.repository.BookmarkRepositoryImpl
import com.base.app.data.repository.PostRepositoryImpl
import com.base.app.data.repository.UserPreferencesRepositoryImpl
import com.base.app.domain.repository.BookmarkRepository
import com.base.app.domain.repository.PostRepository
import com.base.app.domain.repository.UserPreferencesRepository
import com.base.app.domain.usecase.bookmark.ObserveBookmarkedIdsUseCase
import com.base.app.domain.usecase.bookmark.ObserveBookmarksUseCase
import com.base.app.domain.usecase.bookmark.ToggleBookmarkUseCase
import com.base.app.domain.usecase.post.GetPostUseCase
import com.base.app.domain.usecase.post.GetPostsPageUseCase
import com.base.app.domain.usecase.settings.ObserveThemeUseCase
import com.base.app.domain.usecase.settings.SetThemeUseCase
import com.base.app.data.local.datasource.VideoFeedCacheLocalDataSource
import com.base.app.data.local.datasource.VideoFeedCacheLocalDataSourceImpl
import com.base.app.data.local.datasource.WatchProgressLocalDataSource
import com.base.app.data.local.datasource.WatchProgressLocalDataSourceImpl
import com.base.app.data.remote.datasource.VideoRemoteDataSource
import com.base.app.data.remote.datasource.VideoRemoteDataSourceImpl
import com.base.app.data.remote.http.defaultJson
import com.base.app.data.repository.VideoRepositoryImpl
import com.base.app.data.repository.WatchProgressRepositoryImpl
import com.base.app.domain.repository.VideoRepository
import com.base.app.domain.repository.WatchProgressRepository
import com.base.app.domain.usecase.progress.GetResumePositionUseCase
import com.base.app.domain.usecase.progress.SaveWatchProgressUseCase
import com.base.app.domain.usecase.video.GetVideoFeedUseCase
import com.base.app.domain.usecase.video.PickVideoSourceUseCase
import java.io.File
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/**
 * Thứ tự khai: hạ tầng → datasource → repository → use case. Datasource và repository bind theo
 * interface (`single<X> { XImpl(...) }`); use case là `factory` (không giữ state).
 * Dòng `SAMPLE` thuộc luồng mẫu post, `SAMPLE (video)` thuộc luồng mẫu Reels/Player — xoá cùng luồng mẫu đó.
 */
val dataModule = module {
    // Hạ tầng
    single { ApiConfig(baseUrl = get<AppConfig>().baseUrl) }
    single { provideHttpClient(apiConfig = get(), enableLogging = get<AppConfig>().isDebug) }
    single { Room.databaseBuilder(androidContext(), AppDatabase::class.java, AppDatabase.NAME).build() }
    single { get<AppDatabase>().bookmarkDao() } // SAMPLE
    single { get<AppDatabase>().watchProgressDao() } // SAMPLE (video)

    // Datasource
    single<PostRemoteDataSource> { PostRemoteDataSourceImpl(get()) } // SAMPLE
    single<BookmarkLocalDataSource> { BookmarkLocalDataSourceImpl(get()) } // SAMPLE
    single<VideoRemoteDataSource> { VideoRemoteDataSourceImpl(get()) } // SAMPLE (video)
    single<VideoFeedCacheLocalDataSource> { // SAMPLE (video)
        VideoFeedCacheLocalDataSourceImpl(File(androidContext().filesDir, "feed_cache"), defaultJson)
    }
    single<WatchProgressLocalDataSource> { WatchProgressLocalDataSourceImpl(get()) } // SAMPLE (video)
    single<UserPreferencesLocalDataSource> { UserPreferencesLocalDataSourceImpl(androidContext()) }

    // Repository
    single<PostRepository> { PostRepositoryImpl(get()) } // SAMPLE
    single<BookmarkRepository> { BookmarkRepositoryImpl(get()) } // SAMPLE
    single<VideoRepository> { VideoRepositoryImpl(get(), get()) } // SAMPLE (video)
    single<WatchProgressRepository> { WatchProgressRepositoryImpl(get()) } // SAMPLE (video)
    single<UserPreferencesRepository> { UserPreferencesRepositoryImpl(get()) }
    // AdsConfigRepository + use case ads bind ở adsModule (:core:ads) — impl đọc Remote Config.

    // Use case — mỗi hành động một class (`operator fun invoke`), không giữ state → factory.
    factory { GetPostsPageUseCase(get()) } // SAMPLE
    factory { GetPostUseCase(get()) } // SAMPLE
    factory { ObserveBookmarkedIdsUseCase(get()) } // SAMPLE
    factory { ObserveBookmarksUseCase(get()) } // SAMPLE
    factory { ToggleBookmarkUseCase(get()) } // SAMPLE
    factory { GetVideoFeedUseCase(get()) } // SAMPLE (video)
    factory { PickVideoSourceUseCase() } // SAMPLE (video)
    factory { GetResumePositionUseCase(get()) } // SAMPLE (video)
    factory { SaveWatchProgressUseCase(get()) } // SAMPLE (video)
    factory { ObserveThemeUseCase(get()) }
    factory { SetThemeUseCase(get()) }
}
