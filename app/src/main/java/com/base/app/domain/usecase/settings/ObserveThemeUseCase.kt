package com.base.app.domain.usecase.settings

import com.base.app.domain.model.ThemeMode
import com.base.app.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/** Chỉ phát khi theme đổi — ghi preference khác (ngôn ngữ, taste) không được làm Activity vẽ lại theme. */
class ObserveThemeUseCase(
    private val userPreferencesRepository: UserPreferencesRepository,
) {
    operator fun invoke(): Flow<ThemeMode> = userPreferencesRepository.preferences.map { it.theme }.distinctUntilChanged()
}
