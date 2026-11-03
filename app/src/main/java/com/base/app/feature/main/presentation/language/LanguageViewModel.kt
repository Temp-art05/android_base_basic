package com.base.app.feature.main.presentation.language

import com.base.app.core.ui.foundation.locale.AppLocale
import com.base.app.core.ui.foundation.vm.BaseViewModel

/**
 * VM quyết no-op / chống bấm lặp; Screen gọi `AppLocale.apply` (cần Activity AppCompat).
 * Cờ [LanguageViewState.applying] được gỡ ở `DisplayedLocaleChanged` (Activity không dựng lại, chỉ nhận config
 * change), hoặc ở `ApplyFinished` khi ngôn ngữ hiển thị không đổi (không có config change nào).
 */
class LanguageViewModel : BaseViewModel<LanguageIntent, LanguageViewState, LanguageSingleEvent>() {

    override fun initState() = LanguageViewState(selected = AppLocale.current())

    override fun processIntent(intent: LanguageIntent) {
        when (intent) {
            LanguageIntent.BackClick -> sendEvent(LanguageSingleEvent.NavigateBack)
            is LanguageIntent.SelectLocale -> selectLocale(intent.locale)
            LanguageIntent.ScreenStarted,
            LanguageIntent.DisplayedLocaleChanged -> updateState { copy(selected = AppLocale.current(), applying = false) }
            LanguageIntent.ApplyFinished -> updateState { copy(applying = false) }
        }
    }

    private fun selectLocale(locale: AppLocale) {
        val state = currentState
        if (locale == state.selected || state.applying) return
        // ✓ đổi ngay; hệ thống dựng lại Activity với locale mới.
        updateState { copy(selected = locale, applying = true) }
        sendEvent(LanguageSingleEvent.ApplyLocale(locale))
    }
}
