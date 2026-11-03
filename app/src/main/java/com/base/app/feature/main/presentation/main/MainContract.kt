package com.base.app.feature.main.presentation.main

import com.base.app.core.ui.foundation.vm.SingleEvent
import com.base.app.core.ui.foundation.vm.ViewIntent
import com.base.app.core.ui.foundation.vm.ViewState
import com.base.app.feature.main.presentation.main.model.MainTab

/**
 * @param visitedTabs tab đã từng mở. Tab chưa mở thì chưa compose (không tạo ViewModel, không
 * gọi API); đã mở một lần thì giữ sống tới khi rời Main.
 */
data class MainViewState(
    val selectedTab: MainTab = MainTab.Home,
    val visitedTabs: Set<MainTab> = setOf(MainTab.Home),
) : ViewState

sealed interface MainIntent : ViewIntent {
    data class SelectTab(val tab: MainTab) : MainIntent
}

sealed interface MainSingleEvent : SingleEvent
