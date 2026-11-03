package com.base.app.core.ads.config

import com.base.app.core.analytics.remoteconfig.RemoteKeys
import com.base.app.core.ads.policy.AdsConfig
import com.base.app.core.ads.policy.PlacementConfig
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Test

class AdsConfigParserTest {

    private fun parse(raw: String) = AdsConfigParser.parse(Json.parseToJsonElement(raw))

    @Test
    fun `README sample parses field by field`() {
        val config = parse(
            """
            {
              "enable": true,
              "disabled_versions": ["1.0.0", "1.0.1"],
              "placements": {
                "interstitial_onboarding": { "enable": true },
                "native_home":             { "enable": true },
                "rewarded_bonus":          { "enable": false }
              }
            }
            """,
        )
        assertEquals(
            AdsConfig(
                enable = true,
                disabledVersions = listOf("1.0.0", "1.0.1"),
                placements = mapOf(
                    "interstitial_onboarding" to PlacementConfig(true),
                    "native_home" to PlacementConfig(true),
                    "rewarded_bonus" to PlacementConfig(false),
                ),
            ),
            config,
        )
    }

    @Test
    fun `embedded remote config default is the code default`() {
        assertEquals(AdsConfig.DEFAULT, AdsConfigParser.parse(RemoteKeys.ADS_CONFIG.default))
    }

    @Test
    fun `missing fields fall back to their defaults and unknown keys are ignored`() {
        assertEquals(AdsConfig(), parse("""{"frequency": 3}"""))
        assertEquals(AdsConfig(enable = false), parse("""{"enable": false, "extra": {"a": 1}}"""))
    }

    @Test
    fun `one mistyped placement does not break the others or the kill switch`() {
        val config = parse(
            """{"enable": true, "disabled_versions": ["1.0.0"],
                "placements": {"native_home": false, "rewarded_bonus": {"enable": false}, "interstitial_onboarding": {"enable": "yes"}}}""",
        )
        assertEquals(listOf("1.0.0"), config.disabledVersions)
        assertEquals(mapOf("rewarded_bonus" to PlacementConfig(false)), config.placements)
    }

    @Test
    fun `enable accepts the strings true and false typed on the console`() {
        assertEquals(false, parse("""{"enable": "false"}""").enable)
        assertEquals(true, parse("""{"enable": "TRUE"}""").enable)
        assertEquals(false, parse("""{"placements": {"native_home": {"enable": "false"}}}""").placements["native_home"]?.enable)
    }

    @Test
    fun `mistyped global enable keeps ads on rather than guessing off`() {
        assertEquals(true, parse("""{"enable": 0}""").enable)
        assertEquals(true, parse("""{"enable": "off"}""").enable)
    }

    @Test
    fun `broken disabled versions entries are dropped, strings kept exactly`() {
        assertEquals(listOf("1.0.0 ", "2.0"), parse("""{"disabled_versions": ["1.0.0 ", 2, null, {"v": 1}, "2.0"]}""").disabledVersions)
        assertEquals(emptyList<String>(), parse("""{"disabled_versions": "1.0.0"}""").disabledVersions)
    }

    @Test
    fun `interval between interstitial reads seconds, number or numeric string`() {
        assertEquals(45L, parse("""{"interval_between_interstitial": 45}""").intervalBetweenInterstitialSec)
        assertEquals(30L, parse("""{"interval_between_interstitial": "30"}""").intervalBetweenInterstitialSec)
        assertEquals(0L, parse("""{"interval_between_interstitial": 0}""").intervalBetweenInterstitialSec)
    }

    @Test
    fun `missing, negative or mistyped interval falls back to the default`() {
        val default = AdsConfig().intervalBetweenInterstitialSec
        assertEquals(default, parse("""{}""").intervalBetweenInterstitialSec)
        assertEquals(default, parse("""{"interval_between_interstitial": -5}""").intervalBetweenInterstitialSec)
        assertEquals(default, parse("""{"interval_between_interstitial": "soon"}""").intervalBetweenInterstitialSec)
        assertEquals(default, parse("""{"interval_between_interstitial": 1.5}""").intervalBetweenInterstitialSec)
    }

    @Test
    fun `null or non object falls back to the embedded default`() {
        assertEquals(AdsConfig.DEFAULT, AdsConfigParser.parse(null))
        assertEquals(AdsConfig.DEFAULT, AdsConfigParser.parse(JsonNull))
        assertEquals(AdsConfig.DEFAULT, AdsConfigParser.parse(JsonPrimitive("{broken")))
        assertEquals(AdsConfig.DEFAULT, AdsConfigParser.parseRaw("{not json"))
        assertEquals(AdsConfig(enable = false), AdsConfigParser.parseRaw("""{"enable":false}"""))
    }
}
