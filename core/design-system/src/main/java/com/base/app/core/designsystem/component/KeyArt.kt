package com.base.app.core.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import com.base.app.core.designsystem.theme.AppColors
import kotlin.math.max

/**
 * Poster của show trên nền key art giả (`.art .a1`…`.a6` của mockup). Nền chọn theo [seed] (thường là
 * showId) nên cùng show luôn cùng màu; poster tải được thì phủ lên, 404 / offline thì nền vẫn là một
 * khung ấm thay vì ô đen.
 */
@Composable
fun KeyArt(
    posterUrl: String?,
    seed: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    paletteIndex: Int? = null,
) {
    val palette = remember(seed, paletteIndex) {
        AppColors.Art.palettes[Math.floorMod(paletteIndex ?: seed.hashCode(), AppColors.Art.palettes.size)]
    }
    Box(modifier.drawBehind { drawArt(palette) }) {
        AppAsyncImage(
            model = posterUrl,
            contentDescription = null,
            contentScale = contentScale,
            modifier = Modifier.matchParentSize(),
        )
    }
}

/** `linear-gradient(165deg, start, end 72%)` + quầng sáng `radial-gradient(80% 60% at 68% 16%)`. */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawArt(palette: AppColors.Art.Palette) {
    drawRect(
        Brush.linearGradient(
            0f to palette.start,
            0.72f to palette.end,
            start = Offset(size.width * 0.35f, 0f),
            end = Offset(size.width * 0.65f, size.height),
        ),
    )
    drawRect(
        Brush.radialGradient(
            0f to palette.glow,
            1f to Color.Transparent,
            center = Offset(size.width * 0.68f, size.height * 0.16f),
            radius = max(size.width, size.height) * 0.6f,
        ),
    )
}
