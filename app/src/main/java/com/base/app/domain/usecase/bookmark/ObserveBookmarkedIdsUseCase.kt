package com.base.app.domain.usecase.bookmark

import com.base.app.domain.repository.BookmarkRepository
import kotlinx.coroutines.flow.Flow

class ObserveBookmarkedIdsUseCase(
    private val bookmarkRepository: BookmarkRepository,
) {
    operator fun invoke(): Flow<Set<Int>> = bookmarkRepository.observeBookmarkedIds()
}
