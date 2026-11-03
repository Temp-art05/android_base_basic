package com.base.app.core.ui.foundation.locale

import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Test

class LanguageLabelsTest {

    private fun device(tag: String) = Locale.forLanguageTag(tag)

    @Test
    fun `device language with a translation in its default region uses the picker endonym`() {
        assertEquals("English (U.S.)", LanguageLabels.deviceLanguage(device("en-US")))
        assertEquals("Português (BR)", LanguageLabels.deviceLanguage(device("pt-BR")))
        assertEquals("Deutsch", LanguageLabels.deviceLanguage(device("de-AT")))
        assertEquals("العربية", LanguageLabels.deviceLanguage(device("ar-EG")))
    }

    @Test
    fun `legacy indonesian code still reads as bahasa indonesia`() {
        assertEquals("Bahasa Indonesia", LanguageLabels.deviceLanguage(Locale("in", "ID")))
    }

    @Test
    fun `another region or a language without translation uses the system display name`() {
        assertEquals("English (United Kingdom)", LanguageLabels.deviceLanguage(device("en-GB")))
        assertEquals("日本語 (日本)", LanguageLabels.deviceLanguage(device("ja-JP")))
    }

    @Test
    fun `app language label is what the ui really shows`() {
        assertEquals("العربية", LanguageLabels.appLanguage(AppLocale.Arabic, device = device("en-US")))
        assertEquals("Français", LanguageLabels.appLanguage(AppLocale.System, device = device("fr-FR")))
        // Máy tiếng Nhật, app không có bản dịch → UI rơi về English.
        assertEquals("English (U.S.)", LanguageLabels.appLanguage(AppLocale.System, device = device("ja-JP")))
    }
}
