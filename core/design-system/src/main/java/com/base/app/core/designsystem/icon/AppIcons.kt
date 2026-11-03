package com.base.app.core.designsystem.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

/**
 * Icon vẽ lại từ SVG của mockup (viewBox 24) — `material-icons-core` không có pause, heart,
 * episodes, volume; ký tự chữ (✓) thì hiển thị lệch theo font nên cũng vẽ ở đây.
 * Màu thật lấy từ `tint` của `Icon`, ở đây chỉ cần màu đặc bất kỳ.
 */
object AppIcons {

    /** Tim viền (`.rail` chưa like). */
    val Heart: ImageVector by lazy { stroked("Heart", HEART) }

    /** Tim đặc (`.rail .lk`). */
    val HeartFilled: ImageVector by lazy { filled("HeartFilled", HEART) }

    /** Ô lưới tập (`Eps` trên rail). */
    val Episodes: ImageVector by lazy {
        stroked(
            "Episodes",
            "M6 4h12a2.5 2.5 0 0 1 2.5 2.5v11a2.5 2.5 0 0 1-2.5 2.5H6a2.5 2.5 0 0 1-2.5-2.5v-11A2.5 2.5 0 0 1 6 4z",
            "M3.5 9.5h17M9 9.5V20",
        )
    }

    /** Loa (dòng Autoplay ở SCR-0304). */
    val Volume: ImageVector by lazy { stroked("Volume", "M5 9v6h4l5 4V5L9 9H5z", "M17 9.5a4 4 0 0 1 0 5") }

    /** Dấu chọn (dòng quality/ngôn ngữ đang chọn ở SCR-0304). */
    val Check: ImageVector by lazy { stroked("Check", "M5 12.5l4.5 4.5L19 7.5") }

    val Play: ImageVector by lazy { filled("Play", "M8 5.5v13l10.5-6.5z") }

    val Pause: ImageVector by lazy { filled("Pause", "M7 5h3.5v14H7zM13.5 5H17v14h-3.5z") }

    /** Bookmark viền (`Save` trên rail Reels, tab Library). */
    val Bookmark: ImageVector by lazy { stroked("Bookmark", BOOKMARK) }

    /** Bookmark đặc — show đã lưu. */
    val BookmarkFilled: ImageVector by lazy { filled("BookmarkFilled", BOOKMARK) }

    // Tab bar `.tb` (stroke 1.7).
    val TabHome: ImageVector by lazy { stroked("TabHome", "M3 10.5 12 3l9 7.5", "M5 9.5V21h14V9.5") }

    val TabReels: ImageVector by lazy {
        stroked(
            "TabReels",
            "M9 3h6a3 3 0 0 1 3 3v12a3 3 0 0 1-3 3H9a3 3 0 0 1-3-3V6a3 3 0 0 1 3-3z",
            "m10.5 9.5 4 2.5-4 2.5z",
        )
    }

    val TabBrowse: ImageVector by lazy {
        stroked("TabBrowse", tile(3.5f, 3.5f), tile(13.5f, 3.5f), tile(3.5f, 13.5f), tile(13.5f, 13.5f))
    }

    val TabLibrary: ImageVector by lazy { stroked("TabLibrary", BOOKMARK) }

    val TabSettings: ImageVector by lazy {
        stroked(
            "TabSettings",
            "M15 12a3 3 0 1 1-6 0a3 3 0 1 1 6 0z",
            "M19.4 15a1.6 1.6 0 0 0 .3 1.8l.1.1a2 2 0 1 1-2.8 2.8l-.1-.1a1.6 1.6 0 0 0-2.7 1.1V21a2 2 0 1 1-4 0v-.1" +
                "A1.6 1.6 0 0 0 7.5 19.4l-.1.1a2 2 0 1 1-2.8-2.8l.1-.1A1.6 1.6 0 0 0 3 14a2 2 0 1 1 0-4h.1" +
                "A1.6 1.6 0 0 0 4.6 7.5l-.1-.1a2 2 0 1 1 2.8-2.8l.1.1A1.6 1.6 0 0 0 10 3a2 2 0 1 1 4 0v.1" +
                "a1.6 1.6 0 0 0 2.7 1.1l.1-.1a2 2 0 1 1 2.8 2.8l-.1.1A1.6 1.6 0 0 0 21 10a2 2 0 1 1 0 4h-.1a1.6 1.6 0 0 0-1.5 1z",
        )
    }

    // App bar / header (`.ico`, `.back`, `.sb`).
    val Search: ImageVector by lazy { stroked("Search", "M18 11a7 7 0 1 1-14 0a7 7 0 1 1 14 0z", "M16.5 16.5 21 21", width = 2f) }

    val Bell: ImageVector by lazy {
        stroked("Bell", "M18 8.5a6 6 0 1 0-12 0c0 6-2.5 7.5-2.5 7.5h17S18 14.5 18 8.5z", "M13.7 20a2 2 0 0 1-3.4 0", width = 1.8f)
    }

    /** Mũi tên back — tự lật khi RTL. */
    val Back: ImageVector by lazy { stroked("Back", "M19 12H5", "m11 18-6-6 6-6", width = 2f, autoMirror = true) }

    val Close: ImageVector by lazy { stroked("Close", "M6.5 6.5l11 11M17.5 6.5l-11 11", width = 2f) }

    val Plus: ImageVector by lazy { stroked("Plus", "M12 5v14M5 12h14", width = 2f) }

    /** `⌄` của nút mở danh sách (vd. dải tập "1–20 ⌄" ở Detail) — ký tự ▾ hiện thành chấm trên một số font. */
    val ChevronDown: ImageVector by lazy { stroked("ChevronDown", "m6 9.5 6 6 6-6", width = 2f) }

    /** `›` cuối hàng — tự lật khi RTL. */
    val ChevronEnd: ImageVector by lazy { stroked("ChevronEnd", "m9.5 6 6 6-6 6", width = 2f, autoMirror = true) }

    /** `↖` của gợi ý tìm kiếm: điền gợi ý vào ô — tự lật khi RTL. */
    val ArrowUpStart: ImageVector by lazy { stroked("ArrowUpStart", "M17 17 7.5 7.5", "M7 15V7h8", width = 1.8f, autoMirror = true) }

    /** Đồng hồ — hàng History của Library (`.r svg` mockup 4.1). */
    val History: ImageVector by lazy { stroked("History", "M20.5 12a8.5 8.5 0 1 1-17 0a8.5 8.5 0 1 1 17 0z", "M12 7.5V12l3 2") }

    /** Wifi gạch chéo — SCR-0603. */
    val WifiOff: ImageVector by lazy {
        stroked(
            "WifiOff",
            "M2 8.5A15 15 0 0 1 22 8.5",
            "M5.5 12.5a10 10 0 0 1 13 0",
            "M9 16.5a5 5 0 0 1 6 0",
            "M13 20a1 1 0 1 1-2 0a1 1 0 1 1 2 0z",
            "M3 3l18 18",
            width = 1.5f,
        )
    }

    /** Vòng tròn chấm than — lỗi không phải mất mạng (cùng layout SCR-0603). */
    val Alert: ImageVector by lazy {
        stroked("Alert", "M20.5 12a8.5 8.5 0 1 1-17 0a8.5 8.5 0 1 1 17 0z", "M12 7.5v5.5", "M12 16.4v.1", width = 1.5f)
    }

    /** Dấu ⋯ cuối hàng History/Saved. */
    val More: ImageVector by lazy { filled("More", "M7.5 12a1.6 1.6 0 1 1-3.2 0a1.6 1.6 0 1 1 3.2 0zM13.6 12a1.6 1.6 0 1 1-3.2 0a1.6 1.6 0 1 1 3.2 0zM19.7 12a1.6 1.6 0 1 1-3.2 0a1.6 1.6 0 1 1 3.2 0z") }

    /** Dấu ✓ chọn của hàng Settings/Language — nét dày hơn [Check]. */
    val Selected: ImageVector by lazy { stroked("Selected", "M5 12.5l4.5 4.5L19 7.5", width = 2.2f) }

    private const val BOOKMARK = "M6.5 3.5h11v17l-5.5-4-5.5 4z"

    private const val HEART = "M12 20s-7-4.6-7-9.6A3.9 3.9 0 0 1 12 7.6a3.9 3.9 0 0 1 7 2.8C19 15.4 12 20 12 20z"

    /** Ô vuông 7×7 bo 1.5 của icon Browse, góc trên-trái ở ([x], [y]). */
    private fun tile(x: Float, y: Float) =
        "M${x + 1.5f} ${y}h4a1.5 1.5 0 0 1 1.5 1.5v4a1.5 1.5 0 0 1-1.5 1.5h-4a1.5 1.5 0 0 1-1.5-1.5v-4a1.5 1.5 0 0 1 1.5-1.5z"

    private fun builder(name: String, autoMirror: Boolean = false) = ImageVector.Builder(
        name = name,
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
        autoMirror = autoMirror,
    )

    private fun stroked(
        name: String,
        vararg paths: String,
        width: Float = 1.7f,
        autoMirror: Boolean = false,
    ): ImageVector = builder(name, autoMirror).apply {
        paths.forEach { path ->
            addPath(
                pathData = addPathNodes(path),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = width,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            )
        }
    }.build()

    private fun filled(name: String, path: String): ImageVector = builder(name).apply {
        addPath(pathData = addPathNodes(path), fill = SolidColor(Color.Black))
    }.build()
}
