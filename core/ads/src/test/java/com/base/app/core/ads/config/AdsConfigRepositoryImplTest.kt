package com.base.app.core.ads.config

import com.base.app.core.analytics.remoteconfig.RemoteConfigKey
import com.base.app.core.analytics.remoteconfig.RemoteConfigSource
import com.base.app.core.analytics.remoteconfig.RemoteKeys
import com.base.app.core.ads.policy.AdsConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertSame
import org.junit.Test

class AdsConfigRepositoryImplTest {

    /** Remote Config giả: `ads_config` do test đặt, đếm số lần đọc. */
    private class FakeRemoteConfig(var adsConfig: JsonElement) : RemoteConfigSource {
        override val isReady: StateFlow<Boolean> = MutableStateFlow(true)
        override suspend fun fetchAndSync(keys: List<RemoteConfigKey<*>>) = true

        @Suppress("UNCHECKED_CAST")
        override fun <T> get(key: RemoteConfigKey<T>): T = if (key == RemoteKeys.ADS_CONFIG) adsConfig as T else key.default
        override fun <T> observe(key: RemoteConfigKey<T>): Flow<T> = flowOf(get(key))
        override suspend fun <T> getAsync(key: RemoteConfigKey<T>): T = get(key)
    }

    private val remote = FakeRemoteConfig(Json.parseToJsonElement("""{"enable": true}"""))
    private val override = AdsDebugOverride()
    private val repository = AdsConfigRepositoryImpl(remote, override)

    @Test
    fun `same raw config is parsed once, a new value is parsed again`() {
        val first = repository.current()
        assertSame(first, repository.current())

        remote.adsConfig = Json.parseToJsonElement("""{"enable": false}""")
        val second = repository.current()
        assertNotSame(first, second)
        assertEquals(AdsConfig(enable = false), second)
    }

    @Test
    fun `debug override wins over remote config`() {
        override.set("""{"enable": false, "disabled_versions": ["9.9"]}""")
        assertEquals(AdsConfig(enable = false, disabledVersions = listOf("9.9")), repository.current())
    }
}
