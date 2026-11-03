package com.base.app.feature.main.presentation.language

import com.base.app.core.ui.foundation.locale.AppLocale
import com.base.app.core.ui.foundation.vm.SingleEvent
import com.base.app.core.ui.foundation.vm.ViewIntent
import com.base.app.core.ui.foundation.vm.ViewState

/**
 * @param applying đã gửi lệnh đổi ngôn ngữ, đang chờ locale mới hiển thị — tap tiếp bị bỏ qua (mỗi lần
 * đổi là một lần dựng lại cả cây UI, spam tap là nháy liên tục).
 */
data class LanguageViewState(
    val selected: AppLocale = AppLocale.System,
    val applying: Boolean = false,
) : ViewState

sealed interface LanguageIntent : ViewIntent {
    data object BackClick : LanguageIntent
    data class SelectLocale(val locale: AppLocale) : LanguageIntent

    /** `ON_START` — đồng bộ lại khi ngôn ngữ app bị đổi từ Settings hệ thống. */
    data object ScreenStarted : LanguageIntent

    /**
     * Locale đang hiển thị (`LocalConfiguration`) đổi — lệnh đổi ngôn ngữ đã áp xong. `MainActivity` tự xử lý
     * `configChanges="locale|layoutDirection"` nên **không** dựng lại, không có `ON_START` để gỡ cờ.
     */
    data object DisplayedLocaleChanged : LanguageIntent

    /** Đã áp xong mà ngôn ngữ hiển thị không đổi — Activity có thể không dựng lại, gỡ cờ ngay. */
    data object ApplyFinished : LanguageIntent
}

sealed interface LanguageSingleEvent : SingleEvent {
    data object NavigateBack : LanguageSingleEvent
    data class ApplyLocale(val locale: AppLocale) : LanguageSingleEvent
}
