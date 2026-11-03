package com.base.app.feature.main.presentation.main.home

import androidx.lifecycle.viewModelScope
import com.base.app.R
import com.base.app.core.ui.foundation.error.toErrorRes
import com.base.app.core.ui.foundation.error.toUiError
import com.base.app.core.ui.foundation.vm.BaseViewModel
import com.base.app.domain.model.Post
import com.base.app.domain.usecase.bookmark.ObserveBookmarkedIdsUseCase
import com.base.app.domain.usecase.bookmark.ToggleBookmarkUseCase
import com.base.app.domain.usecase.post.GetPostsPageUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * SAMPLE — remote (feed, Ktor) + local (bookmark, Room) cùng phục vụ một màn. Load ở `init`, không ở
 * `LaunchedEffect`: đổi tab / xoay máy không load lại.
 */
class HomeViewModel(
    private val getPostsPage: GetPostsPageUseCase,
    private val observeBookmarkedIds: ObserveBookmarkedIdsUseCase,
    private val toggleBookmark: ToggleBookmarkUseCase,
) : BaseViewModel<HomeIntent, HomeViewState, HomeSingleEvent>() {

    private var pageJob: Job? = null

    override fun initState() = HomeViewState()

    init {
        loadFirstPage()
        viewModelScope.launch {
            observeBookmarkedIds().collect { ids -> updateState { copy(bookmarkedIds = ids) } }
        }
    }

    override fun processIntent(intent: HomeIntent) {
        when (intent) {
            HomeIntent.Retry -> loadFirstPage()
            HomeIntent.LoadMore -> loadMore()
            is HomeIntent.PostClick -> sendEvent(HomeSingleEvent.OpenPostDetail(intent.postId))
            is HomeIntent.BookmarkClick -> bookmark(intent.post)
        }
    }

    private fun loadFirstPage() {
        pageJob?.cancel()
        updateState { copy(isLoading = true, error = null) }
        pageJob = viewModelScope.launch {
            getPostsPage(loaded = emptyList())
                .onSuccess { page ->
                    updateState { copy(isLoading = false, posts = page.items, hasMore = page.hasMore, loadMoreFailed = false) }
                }
                .onFailure { error -> updateState { copy(isLoading = false, error = error.toUiError()) } }
        }
    }

    private fun loadMore() {
        val state = currentState
        if (state.isLoading || state.isLoadingMore || !state.hasMore) return
        updateState { copy(isLoadingMore = true, loadMoreFailed = false) }
        pageJob = viewModelScope.launch {
            getPostsPage(loaded = state.posts)
                .onSuccess { page ->
                    updateState { copy(isLoadingMore = false, posts = posts + page.items, hasMore = page.hasMore) }
                }
                .onFailure { updateState { copy(isLoadingMore = false, loadMoreFailed = true) } }
        }
    }

    private fun bookmark(post: Post) {
        val wasBookmarked = post.id in currentState.bookmarkedIds
        viewModelScope.launch {
            toggleBookmark(post)
                .onSuccess {
                    val message = if (wasBookmarked) R.string.bookmark_removed else R.string.bookmark_added
                    sendEvent(HomeSingleEvent.ShowToast(message, success = true))
                }
                .onFailure { error -> sendEvent(HomeSingleEvent.ShowToast(error.toErrorRes(), success = false)) }
        }
    }
}
