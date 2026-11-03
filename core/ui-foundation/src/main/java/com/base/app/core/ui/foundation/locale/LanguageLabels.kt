package com.base.app.core.ui.foundation.locale

import com.base.app.core.ui.foundation.locale.LaunchLanguages
import java.util.Locale

/**
 * Nhãn ngôn ngữ của Settings ("App language") và SCR-0502 ("Currently …"). Hàm thuần nhận `Locale` —
 * Screen đọc locale máy (`LocaleManagerCompat.getSystemLocales`) rồi truyền vào.
 */
object LanguageLabels {

    /** Vùng mặc định của endonym có vùng trong picker ("English (U.S.)", "Português (BR)"). */
    private val DEFAULT_REGION = mapOf("en" to "US", "pt" to "BR")

    /**
     * Ngôn ngữ máy: có bản dịch **và** đúng vùng của endonym (ngôn ngữ khác bỏ qua vùng) → đúng chữ trên
     * picker; còn lại → tên hệ thống viết bằng chính nó (en-GB → "English (United Kingdom)").
     */
    fun deviceLanguage(device: Locale): String {
        val region = DEFAULT_REGION[device.language]
        val regionMatches = region == null || device.country.isEmpty() || device.country == region
        return AppLocale.fromLocale(device)?.endonym?.takeIf { regionMatches }
            ?: device.getDisplayName(device).replaceFirstChar { it.titlecase(device) }
    }

    /**
     * Ngôn ngữ UI đang hiển thị thật: chọn tay → endonym; theo máy → endonym ngôn ngữ máy nếu app có bản
     * dịch, không thì UI rơi về `values/` (English).
     */
    fun appLanguage(selected: AppLocale, device: Locale): String =
        selected.endonym
            ?: AppLocale.fromLocale(device)?.endonym
            ?: LaunchLanguages.endonym(AppLocale.English.tag!!)
}
