package com.base.app.di

import com.base.app.domain.model.ThemeMode
import com.base.app.domain.usecase.settings.ObserveThemeUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

/**
 * Theme Dark/Light của user cho cả process. Single + `Eagerly`: Activity dựng lại (đổi ngôn ngữ, xoay)
 * đọc ngay giá trị đã có thay vì nháy về Dark rồi mới sang Light. Mở lạnh có thể 1 khung Dark trên Splash
 * trước khi DataStore trả — chấp nhận.
 */
class ThemeHolder(observeTheme: ObserveThemeUseCase, scope: CoroutineScope) {
    val theme: StateFlow<ThemeMode> = observeTheme().stateIn(scope, SharingStarted.Eagerly, ThemeMode.Dark)
}
