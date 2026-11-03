package com.base.app.feature.main.presentation.main.settings

import androidx.lifecycle.viewModelScope
import com.base.app.core.ui.foundation.error.toErrorRes
import com.base.app.core.ui.foundation.vm.BaseViewModel
import com.base.app.core.utils.config.AppConfig
import com.base.app.domain.model.ThemeMode
import com.base.app.domain.usecase.settings.ObserveThemeUseCase
import com.base.app.domain.usecase.settings.SetThemeUseCase
import com.base.app.R
import kotlinx.coroutines.launch

/**
 * Ngôn ngữ app không ở đây: `AppLocale` là nguồn sự thật (hệ thống / AppCompat persist), Screen đọc
 * thẳng. Đổi theme làm cả app vẽ lại — trùng hoặc đang ghi thì bỏ qua để tap lặp không nháy.
 */
class SettingsViewModel(
    private val observeTheme: ObserveThemeUseCase,
    private val setTheme: SetThemeUseCase,
    private val appConfig: AppConfig,
) : BaseViewModel<SettingsIntent, SettingsViewState, SettingsSingleEvent>() {

    private var writingTheme: ThemeMode? = null

    override fun initState() = SettingsViewState(versionName = appConfig.versionName, versionCode = appConfig.versionCode)

    init {
        viewModelScope.launch {
            observeTheme().collect { theme -> updateState { copy(theme = theme) } }
        }
    }

    override fun processIntent(intent: SettingsIntent) {
        when (intent) {
            SettingsIntent.OpenLanguage -> sendEvent(SettingsSingleEvent.OpenLanguage)
            is SettingsIntent.SelectTheme -> selectTheme(intent.theme)
        }
    }

    private fun selectTheme(theme: ThemeMode) {
        if (theme == currentState.theme || writingTheme != null) return
        val previous = currentState.theme
        writingTheme = theme
        // ✓ đổi ngay, không chờ DataStore. Ghi lỗi thì trả ✓ về: theme thật vẫn là cái cũ và
        // ObserveThemeUseCase (distinctUntilChanged) sẽ không phát lại để tự sửa.
        updateState { copy(theme = theme) }
        viewModelScope.launch {
            setTheme(theme).onFailure { error ->
                updateState { copy(theme = previous) }
                sendEvent(SettingsSingleEvent.ShowToast(error.toErrorRes(), success = false))
            }
            writingTheme = null
        }
    }
}
