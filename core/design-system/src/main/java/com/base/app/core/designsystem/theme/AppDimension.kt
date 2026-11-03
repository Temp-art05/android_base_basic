package com.base.app.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Spacing `--s1`…`--s6` của mockup (bước 4dp) và vài kích thước cố định của design. */
@Immutable
data class AppDimension(
    val s1: Dp = 4.dp,
    val s2: Dp = 8.dp,
    val s3: Dp = 12.dp,
    val s4: Dp = 16.dp,
    val s5: Dp = 20.dp,
    val s6: Dp = 24.dp,

    /** Lề ngang chuẩn của màn. */
    val screenGutter: Dp = 16.dp,

    /** Vùng chạm tối thiểu — design chốt 48dp cho Android. */
    val touchTarget: Dp = 48.dp,

    val buttonHeight: Dp = 46.dp,
    val iconButton: Dp = 40.dp,
    val icon: Dp = 22.dp,
    val tabIcon: Dp = 24.dp,

    /** Ô tập `.ep` (SCR-0301/0303). */
    val episodeCell: Dp = 48.dp,
    val episodeLock: Dp = 11.dp,
    /** Icon rail player `.rail svg`. */
    val railIcon: Dp = 29.dp,

    /** Phần nội dung của tab bar `.tb` (padding 9 + item 50 + 4, viền 1) — chưa gồm inset điều hướng. */
    val tabBar: Dp = 64.dp,
    val tabItemMinHeight: Dp = 50.dp,
    /** Khoảng icon ↔ nhãn của tab bar. */
    val tabItemGap: Dp = 6.dp,
    /** Hero Home Day Cycle. */
    val homeHero: Dp = 238.dp,
    /** Tầng preview cố định của Browse. */
    val previewDock: Dp = 378.dp,
    /** Thẻ poster trong rail ngang Home. */
    val railPosterWidth: Dp = 104.dp,
    val railPosterHeight: Dp = 138.dp,
    /** Ô tìm `.sb`. */
    val searchBar: Dp = 44.dp,
    /** Thumbnail hàng `.li`. */
    val listThumbWidth: Dp = 98.dp,
    val listThumbHeight: Dp = 60.dp,
    /** Logo FL-8 ở Splash. */
    val splashMark: Dp = 78.dp,
    /** Nút CTA của onboarding / sheet. */
    val ctaHeight: Dp = 50.dp,
)

val LocalAppDimension = staticCompositionLocalOf { AppDimension() }
