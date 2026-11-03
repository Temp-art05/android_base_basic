package com.base.app.core.designsystem.component

import android.provider.Settings
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.base.app.core.designsystem.theme.AppTheme

/** Tiến độ shimmer chung (0..1) — null = Reduce Motion, khối tĩnh. */
private val LocalShimmerProgress = staticCompositionLocalOf<State<Float>?> { null }

private const val SHIMMER_MS = 1_400
private val ShimmerTile = 320.dp

/**
 * Bọc một khung tải (SCR-0601): mọi [SkeletonBlock] bên trong chạy **cùng pha** như `.sk` của mockup
 * (`background-size: 320px`, `shim 1.4s linear infinite`). Reduce Motion (animator scale = 0) → không chạy.
 */
@Composable
fun SkeletonHost(content: @Composable () -> Unit) {
    val progress = if (rememberReduceMotion()) {
        null
    } else {
        rememberInfiniteTransition(label = "shimmer").animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(SHIMMER_MS, easing = LinearEasing), RepeatMode.Restart),
            label = "shimmerProgress",
        )
    }
    CompositionLocalProvider(LocalShimmerProgress provides progress, content = content)
}

/**
 * Một khối `.sk`: gradient `surface → surface-3 → surface` lặp mỗi 320dp. Đọc tiến độ ở pha vẽ
 * (`drawBehind`) nên shimmer không làm recompose.
 */
@Composable
fun SkeletonBlock(modifier: Modifier = Modifier, shape: Shape = AppTheme.shapes.sm) {
    val progress = LocalShimmerProgress.current
    val base = MaterialTheme.colorScheme.surface
    val highlight = MaterialTheme.colorScheme.surfaceContainerHighest
    Box(
        modifier
            .clip(shape)
            .drawBehind {
                val tile = ShimmerTile.toPx()
                // CSS: background-position chạy -320 → 320, tức trượt đúng 2 ô.
                val shift = (progress?.value ?: 0.5f) * 2 * tile - tile
                drawRect(
                    Brush.linearGradient(
                        colors = listOf(base, highlight, base),
                        start = Offset(shift, 0f),
                        end = Offset(shift + tile, 0f),
                        tileMode = TileMode.Repeated,
                    ),
                )
            },
    )
}

/**
 * "Remove animations" của hệ thống (`ANIMATOR_DURATION_SCALE == 0`). Đọc lại mỗi ON_RESUME vì user đổi
 * trong Settings rồi quay lại app mà Activity không recreate.
 */
@Composable
fun rememberReduceMotion(): Boolean {
    val resolver = LocalContext.current.contentResolver
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    var reduce by remember { mutableStateOf(readReduceMotion(resolver)) }
    DisposableEffect(lifecycle, resolver) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) reduce = readReduceMotion(resolver)
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }
    return reduce
}

private fun readReduceMotion(resolver: android.content.ContentResolver): Boolean =
    Settings.Global.getFloat(resolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
