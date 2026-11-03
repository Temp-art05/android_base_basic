package com.base.app.feature.main.di

import com.base.app.feature.main.presentation.language.LanguageViewModel
import com.base.app.feature.main.presentation.main.MainViewModel
import com.base.app.feature.main.presentation.main.bookmarks.BookmarksViewModel
import com.base.app.feature.main.presentation.main.home.HomeViewModel
import com.base.app.feature.main.presentation.main.settings.SettingsViewModel
import com.base.app.feature.main.presentation.postdetail.PostDetailViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/** ViewModel của feature `main`. Feature mới: thêm package `feature/<name>/` với `di/<Name>Module.kt` riêng. */
val mainModule = module {
    viewModelOf(::MainViewModel)
    viewModelOf(::HomeViewModel)
    viewModelOf(::BookmarksViewModel)
    viewModelOf(::SettingsViewModel)
    viewModelOf(::PostDetailViewModel)
    viewModelOf(::LanguageViewModel)
}
