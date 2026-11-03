package com.base.app.core.ui.foundation.locale

import java.util.Locale

/**
 * Ngôn ngữ UI app có bản dịch (`AppLocale`). Thêm locale: sửa đây + `AppLocale` + `locales_config.xml` + `localeFilters`.
 */
object LaunchLanguages {
    val codes: List<String> = listOf("en", "hi", "ta", "id", "ar", "fr", "es", "pt-BR", "tr", "de")

    /** Endonym cố định (locale máy có thể không có tên chuẩn); mã lạ hỏi `Locale`. */
    fun endonym(code: String): String = ENDONYMS[code] ?: ENDONYMS[code.languageSubtag()]
        ?: Locale.forLanguageTag(code).let { locale ->
            locale.getDisplayName(locale).replaceFirstChar { it.titlecase(locale) }.ifBlank { code }
        }

    private val ENDONYMS = mapOf(
        "en" to "English (U.S.)",
        "hi" to "हिन्दी",
        "ta" to "தமிழ்",
        "id" to "Bahasa Indonesia",
        "ar" to "العربية",
        "fr" to "Français",
        "es" to "Español",
        "pt-BR" to "Português (BR)",
        "tr" to "Türkçe",
        "de" to "Deutsch",
    )
}

/** `pt-BR` → `pt`. Java/Android trả mã cũ `in` cho tiếng Indonesia (`Locale.getLanguage()`), danh sách dùng `id`. */
internal fun String.languageSubtag(): String = substringBefore('-').substringBefore('_').lowercase()
    .let { if (it == "in") "id" else it }
