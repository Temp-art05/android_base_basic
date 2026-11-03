package com.base.app.domain.usecase.bookmark

import com.base.app.domain.model.Post
import com.base.app.domain.repository.BookmarkRepository

class ToggleBookmarkUseCase(
    private val bookmarkRepository: BookmarkRepository,
) {
    suspend operator fun invoke(post: Post): Result<Unit> = bookmarkRepository.toggleBookmark(post)
}
