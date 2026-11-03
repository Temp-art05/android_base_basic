package com.base.app.core.designsystem.ext

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role

/** Clickable không ripple. `interactionSource = null` để Compose tự tạo lười, không cần `composed`. */
fun Modifier.clickableNoRipple(
    enabled: Boolean = true,
    onClickLabel: String? = null,
    role: Role? = null,
    onClick: () -> Unit,
): Modifier = clickable(
    interactionSource = null,
    indication = null,
    enabled = enabled,
    onClickLabel = onClickLabel,
    role = role,
    onClick = onClick,
)

/** Clickable thu nhỏ khi nhấn. Scale đọc trong `graphicsLayer` nên chỉ vẽ lại, không recompose. */
@Composable
fun Modifier.clickableWithScale(
    enabled: Boolean = true,
    scaleDown: Float = 0.95f,
    onClick: () -> Unit,
): Modifier {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) scaleDown else 1f,
        animationSpec = tween(durationMillis = 100),
        label = "scale",
    )
    return this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .clickable(
            interactionSource = interactionSource,
            indication = null,
            enabled = enabled,
            onClick = onClick,
        )
}

/** Áp modifier khi [condition] đúng. */
inline fun Modifier.applyIf(
    condition: Boolean,
    block: Modifier.() -> Modifier,
): Modifier = if (condition) block() else this

/** Áp modifier khi [value] khác null. */
inline fun <T> Modifier.applyIfNotNull(
    value: T?,
    block: Modifier.(T) -> Modifier,
): Modifier = if (value != null) block(value) else this

fun Modifier.horizontalGradient(colors: List<Color>): Modifier =
    background(brush = Brush.horizontalGradient(colors = colors))

fun Modifier.verticalGradient(colors: List<Color>): Modifier =
    background(brush = Brush.verticalGradient(colors = colors))
