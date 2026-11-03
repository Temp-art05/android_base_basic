package com.base.app.core.ui.foundation.format

import android.icu.text.CompactDecimalFormat
import java.text.NumberFormat
import java.util.Locale

/**
 * Số lượt xem/thích dạng gọn theo locale ("4.7M", "47 लाख"…). Dùng ICU của Android vì có từ API 24;
 * `java.text.CompactNumberFormat` là Java 12 nên không có trên minSdk. Âm (dữ liệu hỏng) hiện 0.
 */
fun formatCompactCount(count: Long, locale: Locale): String =
    formatters.get()!!.getOrPut(locale) {
        CompactDecimalFormat.getInstance(locale, CompactDecimalFormat.CompactStyle.SHORT)
    }.format(count.coerceAtLeast(0))

// getInstance tốn và bị gọi trong từng item list/Reels; instance ICU không thread-safe nên cache theo thread.
private val formatters = ThreadLocal.withInitial { HashMap<Locale, CompactDecimalFormat>() }

/**
 * Số nguyên đầy đủ theo locale — cùng bộ chữ số với `%d` trong string resource (`ar` → "٥"), nên số đứng
 * riêng không lệch với số trong câu bên cạnh. `Int.toString()` luôn ra chữ số Latin.
 */
fun formatCount(count: Long, locale: Locale): String =
    integerFormatters.get()!!.getOrPut(locale) { NumberFormat.getIntegerInstance(locale) }.format(count)

private val integerFormatters = ThreadLocal.withInitial { HashMap<Locale, NumberFormat>() }
