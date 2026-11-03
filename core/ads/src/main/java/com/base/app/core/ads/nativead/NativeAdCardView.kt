package com.base.app.core.ads.nativead

import android.content.Context
import android.graphics.Outline
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.InsetDrawable
import android.text.TextUtils
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.base.app.core.ads.R

private const val MATCH = ViewGroup.LayoutParams.MATCH_PARENT
private const val WRAP = ViewGroup.LayoutParams.WRAP_CONTENT
private const val ICON_DP = 44
private const val TOUCH_TARGET_DP = 48
private const val CTA_VISUAL_DP = 36
private const val RADIUS_CARD_DP = 16
private const val RADIUS_ICON_DP = 10
private const val GAP_DP = 12
private const val BADGE_MARGIN_DP = 10

/** Màu (ARGB) + cỡ chữ (sp) lấy từ token design-system ở phía Compose. */
internal data class NativeAdStyle(
    val surface: Int,
    /** Nút CTA: pill sáng chữ tối — nổi nhưng không lẫn với gradient của nút chính app. */
    val ctaBackground: Int,
    val ctaText: Int,
    /** Nền sau media — lộ ra hai bên / trên dưới khi ảnh không cùng tỉ lệ 16:9. */
    val mediaBackground: Int,
    /** Badge "Ad" đè lên media: nền scrim tối để đọc được trên mọi ảnh. */
    val badgeBackground: Int,
    val badgeText: Int,
    val text1: Int,
    val text2: Int,
    val headlineSp: Float,
    val bodySp: Float,
    val badgeSp: Float,
    val buttonSp: Float,
)

/**
 * Card native kiểu "sponsored" của feed, dựng bằng code (đề cấm XML layout): media 16:9 (FIT_CENTER) tràn viền trên,
 * badge "Ad" đè góc đầu media, dưới là icon + headline + body 2 dòng và CTA dạng pill ở cuối hàng. Card nền
 * `surface` bo 16dp như thẻ nội dung của app. AdChoices do SDK tự đặt góc trên-cuối.
 */
internal class NativeAdCard(private val context: Context, private val style: NativeAdStyle) {

    /** `NativeAdView` là class final nên card bọc nó thay vì kế thừa. */
    val view = NativeAdView(context).also { it.tag = this }

    private val media = MediaView(context)
    private val mediaFrame = AspectRatioFrame(context)
    private val badge = text(style.badgeSp, style.badgeText, bold = true).apply {
        setText(R.string.ads_native_badge)
        background = rounded(style.badgeBackground, dp(RADIUS_ICON_DP).toFloat())
        setPadding(dp(8), dp(2), dp(8), dp(2))
    }
    private val icon = ImageView(context).apply {
        scaleType = ImageView.ScaleType.CENTER_CROP
        clipToOutline = true
        outlineProvider = roundedOutline(dp(RADIUS_ICON_DP))
    }
    private val headline = text(style.headlineSp, style.text1, bold = true).apply { maxLines = 1 }
    private val body = text(style.bodySp, style.text2, bold = false).apply { maxLines = 2 }
    private val cta = text(style.buttonSp, style.ctaText, bold = true).apply {
        maxLines = 1
        gravity = Gravity.CENTER
        textAlignment = View.TEXT_ALIGNMENT_CENTER
        minHeight = dp(TOUCH_TARGET_DP)
        // Vùng chạm 48dp, phần nhìn thấy là pill 36dp ở giữa. Padding đặt SAU background: InsetDrawable mang
        // padding riêng, gán background sẽ đè padding đã đặt trước đó (chữ dính sát mép pill).
        val inset = (dp(TOUCH_TARGET_DP) - dp(CTA_VISUAL_DP)) / 2
        background = InsetDrawable(rounded(style.ctaBackground, dp(CTA_VISUAL_DP) / 2f), 0, inset, 0, inset)
        setPadding(dp(18), 0, dp(18), 0)
    }

    init {
        view.layoutDirection = View.LAYOUT_DIRECTION_LOCALE
        mediaFrame.apply {
            setBackgroundColor(style.mediaBackground)
            addView(media, FrameLayout.LayoutParams(MATCH, MATCH))
            addView(
                badge,
                FrameLayout.LayoutParams(WRAP, WRAP, Gravity.TOP or Gravity.START).apply {
                    marginStart = dp(BADGE_MARGIN_DP)
                    topMargin = dp(BADGE_MARGIN_DP)
                },
            )
        }
        val texts = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            addView(headline, LinearLayout.LayoutParams(MATCH, WRAP))
            addView(body, LinearLayout.LayoutParams(MATCH, WRAP).apply { topMargin = dp(2) })
        }
        val info = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(GAP_DP), dp(GAP_DP), dp(GAP_DP), dp(GAP_DP))
            addView(icon, LinearLayout.LayoutParams(dp(ICON_DP), dp(ICON_DP)).apply { marginEnd = dp(GAP_DP) })
            addView(texts, LinearLayout.LayoutParams(0, WRAP, 1f).apply { marginEnd = dp(GAP_DP) })
            addView(cta, LinearLayout.LayoutParams(WRAP, WRAP))
        }
        val card = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            background = rounded(style.surface, dp(RADIUS_CARD_DP).toFloat())
            clipToOutline = true
            outlineProvider = roundedOutline(dp(RADIUS_CARD_DP))
            addView(mediaFrame, LinearLayout.LayoutParams(MATCH, WRAP))
            addView(info, LinearLayout.LayoutParams(MATCH, WRAP))
        }
        view.addView(card, FrameLayout.LayoutParams(MATCH, WRAP))

        view.headlineView = headline
        view.bodyView = body
        view.iconView = icon
        view.mediaView = media
        view.callToActionView = cta
    }

    /** Ad đang gắn — `AndroidView.update` chạy mỗi lần recompose, cùng ad thì không bind lại. */
    private var bound: NativeAd? = null

    fun bind(ad: NativeAd) {
        if (bound === ad) return
        bound = ad
        headline.text = ad.headline.orEmpty()
        // Không có body thì dòng phụ là tên nhà quảng cáo (vẫn là asset của ad, không tự bịa chữ).
        body.showText(ad.body ?: ad.advertiser)
        cta.showText(ad.callToAction)
        val drawable = ad.icon?.drawable
        icon.setImageDrawable(drawable)
        icon.visibility = if (drawable == null) View.GONE else View.VISIBLE
        // FIT_CENTER trong khung 16:9: ảnh khác tỉ lệ (vuông, dọc) hiện đủ trên nền tối, không bị cắt như CENTER_CROP.
        media.mediaContent = ad.mediaContent
        media.setImageScaleType(ImageView.ScaleType.FIT_CENTER)
        view.setNativeAd(ad)
    }

    companion object {
        fun from(view: NativeAdView): NativeAdCard? = view.tag as? NativeAdCard
    }

    private fun TextView.showText(value: String?) {
        text = value.orEmpty()
        visibility = if (value.isNullOrBlank()) View.GONE else View.VISIBLE
    }

    private fun text(sizeSp: Float, color: Int, bold: Boolean) = TextView(context).apply {
        setTextSize(TypedValue.COMPLEX_UNIT_SP, sizeSp)
        setTextColor(color)
        ellipsize = TextUtils.TruncateAt.END
        textAlignment = View.TEXT_ALIGNMENT_VIEW_START
        if (bold) setTypeface(typeface, Typeface.BOLD)
    }

    private fun rounded(color: Int, radius: Float) = GradientDrawable().apply {
        setColor(color)
        cornerRadius = radius
    }

    private fun roundedOutline(radius: Int) = object : ViewOutlineProvider() {
        override fun getOutline(view: View, outline: Outline) {
            outline.setRoundRect(0, 0, view.width, view.height, radius.toFloat())
        }
    }

    private fun dp(value: Int): Int = (value * context.resources.displayMetrics.density).toInt()

    /** Khung giữ media ở tỉ lệ 16:9 theo bề ngang card — thẻ gọn trong feed. */
    private class AspectRatioFrame(context: Context) : FrameLayout(context) {
        override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
            val width = MeasureSpec.getSize(widthMeasureSpec)
            super.onMeasure(
                MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(width * 9 / 16, MeasureSpec.EXACTLY),
            )
        }
    }
}
