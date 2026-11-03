package com.base.app.core.analytics.remoteconfig

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.json.Json
import timber.log.Timber

private val Context.remoteConfigDataStore by preferencesDataStore(name = "remote_config")

/**
 * Firebase fetch + DataStore (giá trị phiên trước) + cache in-memory.
 *
 * - Single-flight: nhiều nơi cùng gọi [fetchAndSync] chỉ ra một request.
 * - Seed default của [RemoteKeys] vào Firebase: key chưa tạo trên console trả default của code.
 * - Lúc khởi tạo nạp DataStore vào cache, nên [get] có giá trị phiên trước ngay cả khi fetch chưa xong.
 */
class RemoteConfigManager(
    context: Context,
    private val firebaseRemoteConfig: FirebaseRemoteConfig,
    minimumFetchIntervalSeconds: Long = DEFAULT_FETCH_INTERVAL_SECONDS,
    private val json: Json = Json { ignoreUnknownKeys = true },
) : RemoteConfigSource {

    private val dataStore = context.remoteConfigDataStore
    private val cache = ConcurrentHashMap<String, Any?>()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val fetchMutex = Mutex()
    private var inFlight: Deferred<Boolean>? = null

    private val _isReady = MutableStateFlow(false)
    override val isReady: StateFlow<Boolean> = _isReady.asStateFlow()

    init {
        firebaseRemoteConfig.setConfigSettingsAsync(
            remoteConfigSettings {
                fetchTimeoutInSeconds = 60
                // Mặc định SDK là 12h ⇒ kill switch lan rất chậm.
                minimumFetchIntervalInSeconds = minimumFetchIntervalSeconds
            }
        )
        firebaseRemoteConfig.setDefaultsAsync(RemoteKeys.defaultsMap())
        // Fetch về trước thì giữ giá trị mới (putIfAbsent).
        scope.launch { loadFromLocal(RemoteKeys.ALL_KEYS, overwrite = false) }
    }

    override suspend fun fetchAndSync(keys: List<RemoteConfigKey<*>>): Boolean {
        val job = fetchMutex.withLock {
            inFlight?.takeIf { it.isActive } ?: scope.async { doFetch(keys) }.also { inFlight = it }
        }
        return job.await()
    }

    private suspend fun doFetch(keys: List<RemoteConfigKey<*>>): Boolean = runCatching {
        firebaseRemoteConfig.fetchAndActivate().await()
        syncToLocal(keys)
        true
    }.getOrElse { e ->
        Timber.e(e, "Remote config fetch failed")
        loadFromLocal(keys, overwrite = false)
        false
    }.also { _isReady.value = true }

    /** Cache (fetch phiên này hoặc giá trị phiên trước), cuối cùng là default. */
    @Suppress("UNCHECKED_CAST")
    override fun <T> get(key: RemoteConfigKey<T>): T = cache[key.key] as? T ?: key.default

    override fun <T> observe(key: RemoteConfigKey<T>): Flow<T> =
        dataStore.data.map { prefs -> getValue(key, prefs) }

    override suspend fun <T> getAsync(key: RemoteConfigKey<T>): T = observe(key).first()

    private suspend fun syncToLocal(keys: List<RemoteConfigKey<*>>) {
        dataStore.edit { prefs ->
            keys.forEach { key -> prefs.put(key, firebaseRemoteConfig.getString(key.key)) }
        }
        val prefs = dataStore.data.first()
        keys.forEach { key -> cache[key.key] = getValue(key, prefs) }
    }

    private suspend fun loadFromLocal(keys: List<RemoteConfigKey<*>>, overwrite: Boolean) {
        if (keys.isEmpty()) return
        val prefs = dataStore.data.first()
        keys.forEach { key ->
            val value = getValue(key, prefs)
            if (overwrite) cache[key.key] = value else cache.putIfAbsent(key.key, value)
        }
    }

    private fun androidx.datastore.preferences.core.MutablePreferences.put(
        key: RemoteConfigKey<*>,
        rawValue: String,
    ) {
        when (key) {
            is StringKey -> this[key.dataStoreKey] = rawValue
            is LongKey -> this[key.dataStoreKey] = rawValue.toLongOrNull() ?: key.default
            is BooleanKey -> this[key.dataStoreKey] = rawValue.toBooleanStrictOrNull() ?: key.default
            is JsonKey<*> -> this[key.dataStoreKey] = rawValue
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> getValue(key: RemoteConfigKey<T>, prefs: Preferences): T = when (key) {
        is StringKey -> (prefs[key.dataStoreKey] ?: key.default) as T
        is LongKey -> (prefs[key.dataStoreKey] ?: key.default) as T
        is BooleanKey -> (prefs[key.dataStoreKey] ?: key.default) as T
        is JsonKey<T> -> prefs[key.dataStoreKey]
            ?.takeIf { it.isNotEmpty() }
            ?.let { raw -> runCatching { json.decodeFromString(key.serializer, raw) }.getOrNull() }
            ?: key.default
    }

    companion object {
        const val DEFAULT_FETCH_INTERVAL_SECONDS = 3600L
    }
}
