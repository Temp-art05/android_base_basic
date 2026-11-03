package com.base.app.feature.main.presentation.main.bookmarks

import androidx.lifecycle.viewModelScope
import com.base.app.R
import com.base.app.core.ui.foundation.error.toErrorRes
import com.base.app.core.ui.foundation.vm.BaseViewModel
import com.base.app.domain.model.Post
import com.base.app.domain.usecase.bookmark.ObserveBookmarksUseCase
import com.base.app.domain.usecase.bookmark.ToggleBookmarkUseCase
import kotlinx.coroutines.launch

class BookmarksViewModel(
    private val observeBookmarks: ObserveBookmarksUseCase,
    private val toggleBookmark: ToggleBookmarkUseCase,
) : BaseViewModel<BookmarksIntent, BookmarksViewState, BookmarksSingleEvent>() {

    override fun initState() = BookmarksViewState()

    init {
        viewModelScope.launch {
            observeBookmarks().collect { posts -> updateState { copy(posts = posts, isLoading = false) } }
        }
    }

    override fun processIntent(intent: BookmarksIntent) {
        when (intent) {
            is BookmarksIntent.PostClick -> sendEvent(BookmarksSingleEvent.OpenPostDetail(intent.postId))
            is BookmarksIntent.RemoveClick -> remove(intent.post)
            BookmarksIntent.BrowsePostsClick -> sendEvent(BookmarksSingleEvent.OpenHomeTab)
        }
    }

    private fun remove(post: Post) {
        viewModelScope.launch {
            toggleBookmark(post)
                .onSuccess { sendEvent(BookmarksSingleEvent.ShowToast(R.string.bookmark_removed, success = true)) }
                .onFailure { error -> sendEvent(BookmarksSingleEvent.ShowToast(error.toErrorRes(), success = false)) }
        }
    }
}
