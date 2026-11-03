package com.base.app.core.ui.foundation.format

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Locale

// Chuỗi kỳ vọng khoá theo ICU của SDK ghim — đổi sdk thì chạy lại và soát output.
// API 24–27 (ICU < 60) ra khác: 2 chữ số có nghĩa (999 → "1K", 472K → "470K") và hậu tố ar bị cụt
// ("مليو") — chưa xử lý, ghi trong README "việc làm tiếp". Từ 28 trở lên khớp.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28, 35])
class CountFormatterTest {

    private val en = Locale.forLanguageTag("en")

    @Test
    fun `english uses K M B suffixes`() {
        assertEquals("472K", formatCompactCount(472_000, en))
        assertEquals("4.7M", formatCompactCount(4_707_000, en))
        assertEquals("5B", formatCompactCount(5_000_000_000, en))
    }

    @Test
    fun `small numbers stay plain`() {
        assertEquals("0", formatCompactCount(0, en))
        assertEquals("999", formatCompactCount(999, en))
    }

    @Test
    fun `negative counts clamp to zero`() {
        assertEquals("0", formatCompactCount(-12, en))
    }

    @Test
    fun `arabic does not fall back to latin suffix`() {
        val text = formatCompactCount(4_707_000, Locale.forLanguageTag("ar"))
        assertFalse(text, text.contains('M'))
        assertEquals(EXPECTED_AR, text)
    }

    @Test
    fun `hindi groups by lakh`() {
        assertEquals(EXPECTED_HI, formatCompactCount(4_700_000, Locale.forLanguageTag("hi")))
    }

    @Test
    fun `full count uses the same digits as percent-d in strings`() {
        assertEquals("1,234", formatCount(1_234, en))
        val ar = Locale.forLanguageTag("ar")
        assertEquals(String.format(ar, "%d", 5), formatCount(5, ar))
    }

    // ICU chèn no-break space giữa số và đơn vị.
    private companion object {
        const val EXPECTED_AR = "٤٫٧\u00A0مليون"
        const val EXPECTED_HI = "47\u00A0लाख"
    }
}
