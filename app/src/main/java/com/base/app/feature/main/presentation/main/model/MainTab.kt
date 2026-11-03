package com.base.app.feature.main.presentation.main.model

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import com.base.app.core.designsystem.icon.AppIcons
import com.base.app.R

/** Tab của Main. Thêm tab: thêm entry + nhánh trong `MainScreen`. */
enum class MainTab(
    @param:StringRes val labelRes: Int,
    val icon: ImageVector,
) {
    Home(R.string.tab_home, AppIcons.TabHome),
    Reels(R.string.tab_reels, AppIcons.TabReels),
    Bookmarks(R.string.tab_bookmarks, AppIcons.Bookmark),
    Settings(R.string.tab_settings, AppIcons.TabSettings),
}
