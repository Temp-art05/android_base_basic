package com.base.app.core.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/** Radius `--r-sm`…`--r-full` của mockup. */
@Immutable
data class AppShape(
    val sm: Shape = RoundedCornerShape(8.dp),
    val md: Shape = RoundedCornerShape(12.dp),
    val lg: Shape = RoundedCornerShape(16.dp),
    val xl: Shape = RoundedCornerShape(22.dp),
    val full: Shape = RoundedCornerShape(percent = 50),
    val sheet: Shape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp),
    /** Badge nhỏ (TONIGHT'S PICK, NEW). */
    val badge: Shape = RoundedCornerShape(6.dp),
    /** Thẻ thông tin (khung giờ Home, thẻ ping SCR-0005) — 14dp. */
    val card: Shape = RoundedCornerShape(14.dp),
    /** Hero Home — 20dp. */
    val hero: Shape = RoundedCornerShape(20.dp),
    /** Poster lưới Browse — 10dp. */
    val poster: Shape = RoundedCornerShape(10.dp),
    /** Poster rail Home — 11dp. */
    val posterRail: Shape = RoundedCornerShape(11.dp),
)

val LocalAppShape = staticCompositionLocalOf { AppShape() }
