package com.base.app.core.designsystem.theme

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = AppColors.Brand.accent,
    onPrimary = AppColors.Brand.onAccent,
    primaryContainer = AppColors.Dark.surface3,
    onPrimaryContainer = AppColors.Dark.text1,
    secondary = AppColors.Brand.gradientEnd,
    onSecondary = AppColors.Dark.text1,
    background = AppColors.Dark.bg,
    onBackground = AppColors.Dark.text1,
    surface = AppColors.Dark.surface,
    onSurface = AppColors.Dark.text1,
    surfaceVariant = AppColors.Dark.surface2,
    onSurfaceVariant = AppColors.Dark.text2,
    // Sheet/dialog/menu của Material đọc các surfaceContainer* — map hết để không rơi về màu mặc định.
    surfaceContainerLowest = AppColors.Dark.bg,
    surfaceContainerLow = AppColors.Dark.surface,
    surfaceContainer = AppColors.Dark.surface,
    surfaceContainerHigh = AppColors.Dark.surface2,
    surfaceContainerHighest = AppColors.Dark.surface3,
    outline = AppColors.Dark.border,
    outlineVariant = AppColors.Dark.border,
    error = AppColors.Status.error,
    onError = AppColors.Dark.text1,
)

private val LightColorScheme = lightColorScheme(
    primary = AppColors.Brand.accentPress,
    onPrimary = AppColors.Brand.onAccent,
    primaryContainer = AppColors.Light.surface3,
    onPrimaryContainer = AppColors.Light.text1,
    secondary = AppColors.Brand.gradientEnd,
    onSecondary = AppColors.Light.surface,
    background = AppColors.Light.bg,
    onBackground = AppColors.Light.text1,
    surface = AppColors.Light.surface,
    onSurface = AppColors.Light.text1,
    surfaceVariant = AppColors.Light.surface2,
    onSurfaceVariant = AppColors.Light.text2,
    // Sheet/dialog/menu của Material đọc các surfaceContainer* — map hết để không rơi về màu mặc định.
    surfaceContainerLowest = AppColors.Light.bg,
    surfaceContainerLow = AppColors.Light.surface,
    surfaceContainer = AppColors.Light.surface,
    surfaceContainerHigh = AppColors.Light.surface2,
    surfaceContainerHighest = AppColors.Light.surface3,
    outline = AppColors.Light.border,
    outlineVariant = AppColors.Light.border,
    error = AppColors.Status.error,
    onError = AppColors.Light.surface,
)

/**
 * Chủ ý KHÔNG bật dynamic color (Material You): màu hệ thống sẽ ghi đè brand Sunset Flow.
 * Mặc định dark, không theo hệ thống: design chọn sẵn Dark ở Settings và không có "Match system".
 * Light dùng bộ token tạm (xem [AppColors.Light]); lựa chọn Dark/Light của user nối vào ở Phase 6.
 */
@Composable
fun AppTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val extendedColors = if (darkTheme) DarkExtendedColors else LightExtendedColors
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            // Không set window.statusBarColor: deprecated từ API 35 và no-op khi edge-to-edge.
            // Chỉ cần báo cho hệ thống biết nên vẽ icon status bar sáng hay tối.
            val window = view.context.findActivity()?.window ?: return@SideEffect
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(
        LocalAppShape provides AppShape(),
        LocalAppDimension provides AppDimension(),
        LocalAppExtendedColors provides extendedColors,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            content = content
        )
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

object AppTheme {
    val colors: AppExtendedColors
        @Composable
        @ReadOnlyComposable
        get() = LocalAppExtendedColors.current

    val shapes: AppShape
        @Composable
        @ReadOnlyComposable
        get() = LocalAppShape.current

    val dimensions: AppDimension
        @Composable
        @ReadOnlyComposable
        get() = LocalAppDimension.current
}
