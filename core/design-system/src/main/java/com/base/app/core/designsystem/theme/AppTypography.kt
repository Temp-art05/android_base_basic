package com.base.app.core.designsystem.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

/**
 * Type scale theo mockup. Font hệ thống (mockup dùng system sans) chứ không nhúng font riêng:
 * app hiện cả tên phim tiếng Ả Rập, Hindi, Tamil — font hệ thống có sẵn glyph cho mọi script đó.
 */
object AppTypography {

    private val family = FontFamily.Default

    /** Tiêu đề hero, tên phim ở màn chi tiết — 30sp. */
    val display = TextStyle(
        fontFamily = family,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 30.sp,
        lineHeight = 34.sp,
        letterSpacing = (-0.02).em,
    )

    /** Wordmark FLOW DRAMA — 20sp, giãn chữ rộng. */
    val wordmark = TextStyle(
        fontFamily = family,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 20.sp,
        letterSpacing = 0.16.em,
    )

    /** Wordmark ở Splash — 30sp, giãn .2em. */
    val wordmarkSplash = wordmark.copy(fontSize = 30.sp, letterSpacing = 0.2.em)

    /** Tiêu đề onboarding `.ob h1` — 28sp/800. */
    val headline = TextStyle(
        fontFamily = family,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 28.sp,
        lineHeight = 34.sp,
        letterSpacing = (-0.01).em,
    )

    /** Tiêu đề khung giờ ở Home ("Night flow") — 23sp/800. */
    val daypart = TextStyle(
        fontFamily = family,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 23.sp,
        lineHeight = 28.sp,
    )

    /** Tên show trên key art (hero Home 19sp, preview Browse 21sp) — đậm 800. */
    val artTitle = TextStyle(
        fontFamily = family,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 19.sp,
        lineHeight = 24.sp,
    )

    /** Eyebrow viết hoa (`STEP 1 / 4`, `TAP A POSTER…`, `FINE-TUNE`) — 11.5sp/700, giãn .1em. */
    val eyebrow = TextStyle(
        fontFamily = family,
        fontWeight = FontWeight.Bold,
        fontSize = 11.5.sp,
        letterSpacing = 0.1.em,
    )

    /** Badge gradient / pill trên art (`TONIGHT'S PICK`, `PREVIEW · auto`) — 10.5sp/800, .08em. */
    val badge = TextStyle(
        fontFamily = family,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 10.5.sp,
        letterSpacing = 0.08.em,
    )

    /** Tiêu đề màn / sheet — 19sp. */
    val title = TextStyle(
        fontFamily = family,
        fontWeight = FontWeight.Bold,
        fontSize = 19.sp,
        lineHeight = 24.sp,
        letterSpacing = (-0.015).em,
    )

    /** Tiêu đề section / rail — 17sp. */
    val titleSmall = TextStyle(
        fontFamily = family,
        fontWeight = FontWeight.Bold,
        fontSize = 17.sp,
        lineHeight = 22.sp,
        letterSpacing = (-0.01).em,
    )

    /** Nhãn nút — 15sp. */
    val button = TextStyle(
        fontFamily = family,
        fontWeight = FontWeight.Bold,
        fontSize = 15.sp,
    )

    val bodyLarge = TextStyle(
        fontFamily = family,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp,
    )

    val body = TextStyle(
        fontFamily = family,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    )

    val bodySemibold = body.copy(fontWeight = FontWeight.SemiBold)

    /** Chip, metadata — 13sp. */
    val label = TextStyle(
        fontFamily = family,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 18.sp,
    )

    val labelSemibold = label.copy(fontWeight = FontWeight.SemiBold)

    val caption = TextStyle(
        fontFamily = family,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
    )

    val captionSemibold = caption.copy(fontWeight = FontWeight.SemiBold)

    /** Badge, eyebrow viết hoa — 11sp. */
    val micro = TextStyle(
        fontFamily = family,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 11.sp,
        letterSpacing = 0.08.em,
    )

    /** Thời gian phát `0:42 / 1:31` — mono để số không nhảy khi chạy. */
    val timecode = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Normal,
        fontSize = 10.5.sp,
    )

    /** Dòng version cuối Settings `v1.0.0 (128)` — mockup 11.5px mono, `text-4`. */
    val versionFooter = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Normal,
        fontSize = 11.5.sp,
    )

    /** Nhãn tab bar — mockup 10.5px/600; ảnh chụp thiết bị nhỏ hơn ~16% nên nâng lên 12sp/700. */
    val tab = TextStyle(
        fontFamily = family,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
    )

    /** `.blank h3` — 18sp/700. */
    val blankTitle = TextStyle(
        fontFamily = family,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        lineHeight = 23.sp,
        letterSpacing = (-0.01).em,
    )

    /** `.blank p` — 13.5sp, line-height 1.6. */
    val blankBody = TextStyle(
        fontFamily = family,
        fontWeight = FontWeight.Normal,
        fontSize = 13.5.sp,
        lineHeight = 21.6.sp,
    )

    /** Nhãn hàng `.r .l` của Settings/Library — 14.5sp. */
    val rowLabel = TextStyle(
        fontFamily = family,
        fontWeight = FontWeight.Normal,
        fontSize = 14.5.sp,
        lineHeight = 20.sp,
    )

    /** Dòng phụ `.r .l small` — 11.5sp. */
    val rowSupporting = TextStyle(
        fontFamily = family,
        fontWeight = FontWeight.Normal,
        fontSize = 11.5.sp,
        lineHeight = 15.sp,
    )

    /** Giá trị `.r .v` — 13sp. */
    val rowValue = TextStyle(
        fontFamily = family,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp,
    )

    /** `.glabel` — 11sp/700 viết hoa, giãn 0.1em. */
    val groupLabel = TextStyle(
        fontFamily = family,
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
        letterSpacing = 0.1.em,
    )

    /** Số trên thẻ `.mt b` của Library — 17sp/700. */
    val statValue = TextStyle(
        fontFamily = family,
        fontWeight = FontWeight.Bold,
        fontSize = 17.sp,
        lineHeight = 22.sp,
    )

    /** Nhãn dưới số `.mt span` — 11sp. */
    val statLabel = TextStyle(
        fontFamily = family,
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
        lineHeight = 14.sp,
    )

    /** Đoạn giải thích dưới header (SCR-0502) — 12.5sp, line-height 1.6. */
    val note = TextStyle(
        fontFamily = family,
        fontWeight = FontWeight.Normal,
        fontSize = 12.5.sp,
        lineHeight = 20.sp,
    )

    /** Nút chữ ở header ("Edit") — 13.5sp. */
    val textAction = TextStyle(
        fontFamily = family,
        fontWeight = FontWeight.Normal,
        fontSize = 13.5.sp,
    )

    /** Nút `.seg a` — 13.5sp/600. */
    val segment = TextStyle(
        fontFamily = family,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.5.sp,
    )
}
