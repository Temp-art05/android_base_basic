package com.base.app.core.ads.nativead

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.viewinterop.AndroidView
import com.github.dinhn.ads.core.AdsSdk
import com.github.dinhn.ads.core.NativeAdPool
import com.google.android.gms.ads.nativead.NativeAd
import com.base.app.core.designsystem.theme.AppTheme
import com.base.app.core.designsystem.theme.AppTypography
import com.base.app.core.ads.policy.AdPlacement
import kotlinx.coroutines.flow.Flow

private const val BADGE_SCRIM_ALPHA = 0.6f

/**
 * Ô native: chỉ vẽ khi placement bật. Pool đang tải lần đầu → shimmer ([NativeAdShimmer]); có ad → card;
 * lỗi / offline / chưa request → không vẽ gì, nên không bao giờ để lại khung trống. Không dùng
 * `AdsNative(layoutId)` của lib vì nó bind layout XML.
 */
@Composable
internal fun NativeAdContent(
    placement: AdPlacement,
    enabledFlow: Flow<Boolean>,
    initiallyEnabled: Boolean,
    request: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val enabled by enabledFlow.collectAsState(initial = initiallyEnabled)
    if (!enabled) return

    // Lib chỉ cho request sau khi SDK init + remote config đã áp; key theo các cờ để request lại khi chúng lật.
    val sdkReady by AdsSdk.ready
    val canRequest by AdsSdk.canRequestAds
    val configReady by AdsSdk.remoteConfigReady
    LaunchedEffect(placement, sdkReady, canRequest, configReady) {
        if (sdkReady && canRequest && configReady) request()
    }

    val poolState by remember(placement) { NativeAdPool.state(placement.id) }.collectAsState()
    val ad = when (val state = poolState) {
        is NativeAdPool.State.Loaded -> state.handle.payload as? NativeAd
        // Lần tải đầu: khung chờ cùng bố cục với card. Tải lại khi đã có ad thì pool giữ ad cũ (vẫn Loaded).
        NativeAdPool.State.Loading -> return NativeAdShimmer(modifier)
        NativeAdPool.State.Idle, NativeAdPool.State.Failed -> null
    } ?: return

    val style = NativeAdStyle(
        surface = MaterialTheme.colorScheme.surface.toArgb(),
        mediaBackground = MaterialTheme.colorScheme.scrim.toArgb(),
        ctaBackground = MaterialTheme.colorScheme.onSurface.toArgb(),
        ctaText = MaterialTheme.colorScheme.background.toArgb(),
        badgeBackground = MaterialTheme.colorScheme.scrim.copy(alpha = BADGE_SCRIM_ALPHA).toArgb(),
        badgeText = AppTheme.colors.media.text.toArgb(),
        text1 = MaterialTheme.colorScheme.onSurface.toArgb(),
        text2 = MaterialTheme.colorScheme.onSurfaceVariant.toArgb(),
        headlineSp = AppTypography.bodySemibold.fontSize.value,
        bodySp = AppTypography.caption.fontSize.value,
        badgeSp = AppTypography.micro.fontSize.value,
        buttonSp = AppTypography.button.fontSize.value,
    )
    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { context -> NativeAdCard(context, style).view },
        update = { view -> NativeAdCard.from(view)?.bind(ad) },
        // Chỉ huỷ View; NativeAd thuộc pool của lib (dùng lại khi quay về Home).
        onRelease = { view -> view.destroy() },
    )
}
