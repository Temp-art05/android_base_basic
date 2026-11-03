package com.base.app.data.repository

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import com.base.app.data.local.datasource.UserPreferencesKeys
import com.base.app.data.local.datasource.UserPreferencesLocalDataSource
import com.base.app.data.util.withLocalCatching
import com.base.app.domain.model.ThemeMode
import com.base.app.domain.model.UserPreferences
import com.base.app.domain.repository.UserPreferencesRepository
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import timber.log.Timber

class UserPreferencesRepositoryImpl(
    private val local: UserPreferencesLocalDataSource,
) : UserPreferencesRepository {

    // File DataStore hỏng/không đọc được → dùng mặc định thay vì làm chết app.
    override val preferences: Flow<UserPreferences> = local.data
        .catch { error ->
            if (error !is IOException) throw error
            Timber.e(error, "Read user preferences failed")
            emit(emptyPreferences())
        }
        .map { it.toDomain() }

    override suspend fun setTheme(theme: ThemeMode) = write(UserPreferencesKeys.THEME, theme.name)

    private suspend fun <T> write(key: Preferences.Key<T>, value: T): Result<Unit> =
        withLocalCatching { local.set(key, value) }

    private fun Preferences.toDomain(): UserPreferences {
        val defaults = UserPreferences()
        return UserPreferences(
            theme = ThemeMode.entries.firstOrNull { it.name == this[UserPreferencesKeys.THEME] } ?: defaults.theme,
        )
    }
}
