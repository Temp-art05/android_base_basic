package com.base.app.feature.main.presentation.main

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.toRoute
import com.base.app.core.ui.foundation.vm.BaseViewModel
import com.base.app.feature.main.presentation.main.model.MainTab

class MainViewModel(
    private val savedStateHandle: SavedStateHandle,
) : BaseViewModel<MainIntent, MainViewState, MainSingleEvent>() {

    override fun initState(): MainViewState {
        val initialTab = savedStateHandle.toRoute<MainScreenProvider.Main>().initialTab
            ?.let { name -> MainTab.entries.firstOrNull { it.name == name } }
            ?: MainTab.Home
        return MainViewState(selectedTab = initialTab, visitedTabs = setOf(initialTab))
    }

    override fun processIntent(intent: MainIntent) {
        when (intent) {
            is MainIntent.SelectTab -> selectTab(intent.tab)
        }
    }

    private fun selectTab(tab: MainTab) {
        if (tab == currentState.selectedTab) return
        updateState { copy(selectedTab = tab, visitedTabs = visitedTabs + tab) }
    }
}
