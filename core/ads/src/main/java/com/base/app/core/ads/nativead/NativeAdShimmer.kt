package com.base.app.core.ads.nativead

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.base.app.core.designsystem.theme.AppTheme

private const val PULSE_MS = 900
private const val PULSE_MIN_ALPHA = 0.45f

/**
 * Khung chờ của ô native, cùng bố cục với [NativeAdCard] (media 16:9 + icon / 2 dòng / CTA pill) để lúc ad về
 * không nhảy layout. Chỉ hiện khi pool đang tải lần đầu; lỗi / offline thì ô biến mất (không để khung trống).
 */
@Composable
internal fun NativeAdShimmer(modifier: Modifier = Modifier) {
    val pulse = rememberInfiniteTransition(label = "nativeShimmer")
    val alpha = pulse.animateFloat(
        initialValue = 1f,
        targetValue = PULSE_MIN_ALPHA,
        animationSpec = infiniteRepeatable(tween(PULSE_MS), RepeatMode.Reverse),
        label = "nativeShimmerAlpha",
    )
    val block = MaterialTheme.colorScheme.surfaceVariant
    Column(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface),
    ) {
        // Đọc alpha ở pha vẽ: nhịp sáng/tối không recompose mỗi frame.
        val pulsing = Modifier.graphicsLayer { this.alpha = alpha.value }
        Box(pulsing.fillMaxWidth().aspectRatio(16f / 9f).background(block))
        Row(Modifier.padding(AppTheme.dimensions.s3), verticalAlignment = Alignment.CenterVertically) {
            Box(pulsing.size(44.dp).clip(RoundedCornerShape(10.dp)).background(block))
            Column(Modifier.weight(1f).padding(horizontal = AppTheme.dimensions.s3)) {
                Box(pulsing.fillMaxWidth(0.6f).height(14.dp).clip(AppTheme.shapes.full).background(block))
                Spacer(Modifier.height(AppTheme.dimensions.s2))
                Box(pulsing.fillMaxWidth(0.9f).height(12.dp).clip(AppTheme.shapes.full).background(block))
            }
            Box(pulsing.width(88.dp).height(36.dp).clip(AppTheme.shapes.full).background(block))
        }
    }
}

@Preview
@Composable
private fun NativeAdShimmerPreview() {
    AppTheme { NativeAdShimmer(Modifier.padding(16.dp)) }
}
