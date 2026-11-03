package com.base.app.domain.model

/**
 * SAMPLE (video) — một mục của feed video dọc. [sources] = quality (`"720p"`) → URL; rỗng = không phát được.
 * Xoá cùng luồng mẫu video (xem `docs/guides/sample-flow.md`).
 */
data class Video(
    val id: String,
    val title: String,
    val description: String,
    val thumbnailUrl: String?,
    val sources: Map<String, String>,
)

/** Nguồn đã chọn để phát. */
data class VideoSource(val quality: String, val url: String)

object Quality {
    /** `"720p"` → 720. Key không có dạng số (`"abc"`) → null, xếp sau mọi key số. */
    fun heightOf(label: String): Int? =
        label.trim().lowercase().removeSuffix("p").toIntOrNull()?.takeIf { it > 0 }

    /** Cao → thấp, key lạ cuối cùng (giữ thứ tự gốc giữa chúng). */
    val HighestFirst: Comparator<String> = compareByDescending<String> { heightOf(it) ?: Int.MIN_VALUE }
}
