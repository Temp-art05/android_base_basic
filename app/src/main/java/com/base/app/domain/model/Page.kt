package com.base.app.domain.model

/** Một trang dữ liệu. [hasMore] do tầng dưới quyết định — ViewModel không tự so `items.size >= PAGE_SIZE`. */
data class Page<T>(
    val items: List<T>,
    val hasMore: Boolean,
)
