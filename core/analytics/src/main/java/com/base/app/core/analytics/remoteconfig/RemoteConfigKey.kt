package com.base.app.core.analytics.remoteconfig

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.serialization.KSerializer

/**
 * Sealed interface cho Remote Config keys.
 * Hỗ trợ: String, Long, Boolean, Json object.
 */
sealed interface RemoteConfigKey<T> {
    val key: String
    val default: T
    val dataStoreKey: Preferences.Key<*>
}

data class StringKey(
    override val key: String,
    override val default: String = "",
    override val dataStoreKey: Preferences.Key<String> = stringPreferencesKey(key)
) : RemoteConfigKey<String>

data class LongKey(
    override val key: String,
    override val default: Long = 0L,
    override val dataStoreKey: Preferences.Key<Long> = longPreferencesKey(key)
) : RemoteConfigKey<Long>

data class BooleanKey(
    override val key: String,
    override val default: Boolean = false,
    override val dataStoreKey: Preferences.Key<Boolean> = booleanPreferencesKey(key)
) : RemoteConfigKey<Boolean>

data class JsonKey<T>(
    override val key: String,
    override val default: T,
    val serializer: KSerializer<T>,
    override val dataStoreKey: Preferences.Key<String> = stringPreferencesKey(key)
) : RemoteConfigKey<T>
