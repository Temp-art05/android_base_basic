package com.base.app.domain.usecase.settings

import com.base.app.domain.model.ThemeMode
import com.base.app.domain.repository.UserPreferencesRepository

class SetThemeUseCase(
    private val userPreferencesRepository: UserPreferencesRepository,
) {
    suspend operator fun invoke(theme: ThemeMode): Result<Unit> = userPreferencesRepository.setTheme(theme)
}
