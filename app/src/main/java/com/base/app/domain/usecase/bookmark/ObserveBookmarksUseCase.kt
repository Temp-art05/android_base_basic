package com.base.app.domain.usecase.bookmark

import com.base.app.domain.model.Post
import com.base.app.domain.repository.BookmarkRepository
import kotlinx.coroutines.flow.Flow

class ObserveBookmarksUseCase(
    private val bookmarkRepository: BookmarkRepository,
) {
    operator fun invoke(): Flow<List<Post>> = bookmarkRepository.observeBookmarks()
}
