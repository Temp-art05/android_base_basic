package com.base.app.core.designsystem.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.base.app.core.designsystem.theme.AppColors
import com.base.app.core.designsystem.theme.AppTheme
import com.base.app.core.designsystem.theme.AppTypography

/**
 * Logo FL-8 "Next Play" (mockup `svg.mk`, viewBox 100): play trắng phía trước, play gradient phía sau
 * lệch dọc — "tập kế đã chờ sẵn". Vẽ bằng path thật, không dùng ảnh.
 */
@Composable
fun FlowMark(modifier: Modifier = Modifier, size: Dp = 23.dp) {
    val back = remember { triangle(46f, 27f, 84f, 50f, 46f, 73f) }
    val front = remember { triangle(18f, 31f, 52f, 50f, 18f, 69f) }
    Canvas(modifier.size(size)) {
        val scale = this.size.minDimension / VIEWBOX
        scale(scale, pivot = Offset.Zero) {
            // `#rvg`: gradient chéo theo khung của play sau.
            val gradient = Brush.linearGradient(
                0f to AppColors.Brand.gradientStart,
                0.55f to AppColors.Brand.gradientMid,
                1f to AppColors.Brand.gradientEnd,
                start = Offset(46f, 27f),
                end = Offset(84f, 73f),
            )
            val stroke = Stroke(width = STROKE, join = StrokeJoin.Round)
            drawPath(back, gradient, alpha = 0.9f)
            drawPath(back, gradient, alpha = 0.9f, style = stroke)
            drawPath(front, Color.White)
            drawPath(front, Color.White, style = stroke)
        }
    }
}

/**
 * Wordmark `.wm`: mark + "FLOW" trắng + "DRAMA" tô gradient. Bản Splash ([splash]): không mark, chữ
 * 30sp giãn .2em, "DRAMA" màu accent đặc (mockup `.splash .w em`).
 */
@Composable
fun Wordmark(modifier: Modifier = Modifier, splash: Boolean = false) {
    val style: TextStyle = if (splash) AppTypography.wordmarkSplash else AppTypography.wordmark
    val dramaStyle = if (splash) {
        SpanStyle(color = MaterialTheme.colorScheme.primary)
    } else {
        SpanStyle(brush = AppTheme.colors.gradientCta)
    }
    val text = remember(dramaStyle, splash) {
        buildAnnotatedString {
            append(FLOW)
            if (!splash) append(WORD_GAP)
            withStyle(dramaStyle) { append(DRAMA) }
        }
    }
    Row(
        modifier = modifier.clearAndSetSemantics { contentDescription = "$FLOW$DRAMA" },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppTheme.dimensions.s2),
    ) {
        if (!splash) FlowMark()
        Text(text = text, style = style, color = MaterialTheme.colorScheme.onBackground, maxLines = 1)
    }
}

private fun triangle(x1: Float, y1: Float, x2: Float, y2: Float, x3: Float, y3: Float) = Path().apply {
    moveTo(x1, y1)
    lineTo(x2, y2)
    lineTo(x3, y3)
    close()
}

private const val VIEWBOX = 100f
private const val STROKE = 5.5f

// Tên thương hiệu, không dịch.
private const val FLOW = "FLOW"
private const val DRAMA = "DRAMA"
private const val WORD_GAP = " "

@Preview
@Composable
private fun WordmarkPreview() {
    AppTheme { Wordmark() }
}
