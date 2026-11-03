package com.base.app.feature.main.presentation.main.settings

import androidx.annotation.StringRes
import com.base.app.core.ui.foundation.vm.SingleEvent
import com.base.app.core.ui.foundation.vm.ViewIntent
import com.base.app.core.ui.foundation.vm.ViewState
import com.base.app.domain.model.ThemeMode

data class SettingsViewState(
    val theme: ThemeMode = ThemeMode.Dark,
    val versionName: String = "",
    val versionCode: Int = 0,
) : ViewState

sealed interface SettingsIntent : ViewIntent {
    data object OpenLanguage : SettingsIntent
    data class SelectTheme(val theme: ThemeMode) : SettingsIntent
}

sealed interface SettingsSingleEvent : SingleEvent {
    data object OpenLanguage : SettingsSingleEvent
    data class ShowToast(@param:StringRes val messageRes: Int, val success: Boolean) : SettingsSingleEvent
}
