package com.base.app.domain.model

/**
 * SAMPLE — model domain của luồng "post". Không mang annotation Ktor/Room: domain không biết dữ liệu
 * được truyền hay lưu thế nào. Xoá cùng luồng mẫu khi làm project thật.
 */
data class Post(
    val id: Int,
    val title: String,
    val body: String,
    val tags: List<String> = emptyList(),
)
