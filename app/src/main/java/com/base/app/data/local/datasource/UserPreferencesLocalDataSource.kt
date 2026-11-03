package com.base.app.data.local.datasource

import android.content.Context
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow

// File hỏng → thay bằng rỗng. Không có handler thì `.catch` chỉ cứu luồng đọc, còn mọi lần `edit`
// ném CorruptionException mãi (preference không bao giờ lưu được).
private val Context.userPreferencesDataStore by preferencesDataStore(
    name = "user_prefs",
    corruptionHandler = ReplaceFileCorruptionHandler { emptyPreferences() },
)

interface UserPreferencesLocalDataSource {
    val data: Flow<Preferences>

    suspend fun <T> set(key: Preferences.Key<T>, value: T)

    suspend fun <T> remove(key: Preferences.Key<T>)
}

class UserPreferencesLocalDataSourceImpl(
    context: Context,
) : UserPreferencesLocalDataSource {
    private val dataStore = context.userPreferencesDataStore

    override val data: Flow<Preferences> = dataStore.data

    override suspend fun <T> set(key: Preferences.Key<T>, value: T) {
        dataStore.edit { it[key] = value }
    }

    override suspend fun <T> remove(key: Preferences.Key<T>) {
        dataStore.edit { it.remove(key) }
    }
}

object UserPreferencesKeys {
    val THEME = stringPreferencesKey("theme")
}
