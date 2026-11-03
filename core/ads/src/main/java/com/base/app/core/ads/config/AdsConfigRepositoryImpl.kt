package com.base.app.core.ads.config

import com.base.app.core.analytics.remoteconfig.RemoteConfigSource
import com.base.app.core.analytics.remoteconfig.RemoteKeys
import com.base.app.core.ads.policy.AdsConfig
import com.base.app.core.ads.policy.AdsConfigRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.JsonElement
import timber.log.Timber

class AdsConfigRepositoryImpl(
    private val remoteConfig: RemoteConfigSource,
    private val debugOverride: AdsDebugOverride,
) : AdsConfigRepository {

    override val config: Flow<AdsConfig> = combine(
        remoteConfig.observe(RemoteKeys.ADS_CONFIG)
            .map(AdsConfigParser::parse)
            .catch { e ->
                Timber.tag(AdsConfigParser.TAG).w(e, "ads_config unreadable, using embedded default")
                emit(AdsConfig.DEFAULT)
            },
        debugOverride.config,
    ) { remote, override -> override ?: remote }
        .distinctUntilChanged()

    /** Parse lần gần nhất theo giá trị raw — `current()` chạy trước mỗi preload/show, không parse lại JSON cũ. */
    @Volatile
    private var lastParsed: Pair<JsonElement, AdsConfig>? = null

    override fun current(): AdsConfig = debugOverride.config.value ?: runCatching {
        val raw = remoteConfig.get(RemoteKeys.ADS_CONFIG)
        lastParsed?.takeIf { it.first == raw }?.second
            ?: AdsConfigParser.parse(raw).also { lastParsed = raw to it }
    }.getOrDefault(AdsConfig.DEFAULT)
}
