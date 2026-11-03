package com.base.app.domain.repository

import com.base.app.domain.model.ThemeMode
import com.base.app.domain.model.UserPreferences
import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    val preferences: Flow<UserPreferences>

    suspend fun setTheme(theme: ThemeMode): Result<Unit>
}
