package com.base.app.core.ads.nativead

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.base.app.core.ads.safely
import com.base.app.core.ads.sdk.AdsSdkFacade
import com.base.app.core.ads.policy.AdPlacement
import com.base.app.core.ads.policy.IsAdPlacementEnabledUseCase
import com.base.app.core.ads.policy.ObserveAdPlacementUseCase

/**
 * Native theo từng vị trí (như `NativeAdManager` của HomeMorph). Card dựng bằng code ([NativeAdCard]) thay vì layout
 * XML của HomeMorph — đề cấm XML layout.
 */
interface NativeAdManager {

    enum class AdType(val placement: AdPlacement, val adUnitId: String) {
        /** Home, sau rail "Pick up your flow" — hoà vào feed, Home không phát video. */
        HOME(AdPlacement.NativeHome, "ca-app-pub-3940256099942544/2247696110"),
    }

    fun register()

    fun isEnabled(type: AdType): Boolean

    /** Ô native; tắt hoặc chưa có ad → không vẽ gì (không giữ chỗ trống). */
    @Composable
    fun Slot(type: AdType, modifier: Modifier)
}

class NativeAdManagerImpl(
    context: Context,
    private val sdk: AdsSdkFacade,
    private val isPlacementEnabled: IsAdPlacementEnabledUseCase,
    private val observePlacementEnabled: ObserveAdPlacementUseCase,
) : NativeAdManager {

    private val appContext: Context = context.applicationContext ?: context

    override fun register() = NativeAdManager.AdType.entries.forEach { type ->
        safely("register ${type.placement.id}") { sdk.registerNative(type.placement.id, type.adUnitId) }
    }

    override fun isEnabled(type: NativeAdManager.AdType): Boolean = isPlacementEnabled(type.placement)

    @Composable
    override fun Slot(type: NativeAdManager.AdType, modifier: Modifier) {
        NativeAdContent(
            placement = type.placement,
            enabledFlow = remember(type) { observePlacementEnabled(type.placement) },
            initiallyEnabled = remember(type) { isEnabled(type) },
            request = { safely("native ${type.placement.id}") { sdk.requestNative(appContext, type.placement.id) } },
            modifier = modifier,
        )
    }
}
