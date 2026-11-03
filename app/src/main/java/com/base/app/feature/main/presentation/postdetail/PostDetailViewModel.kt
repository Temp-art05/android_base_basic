package com.base.app.feature.main.presentation.postdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.base.app.R
import com.base.app.core.ui.foundation.error.toErrorRes
import com.base.app.core.ui.foundation.error.toUiError
import com.base.app.core.ui.foundation.vm.BaseViewModel
import com.base.app.domain.usecase.bookmark.ObserveBookmarkedIdsUseCase
import com.base.app.domain.usecase.bookmark.ToggleBookmarkUseCase
import com.base.app.domain.usecase.post.GetPostUseCase
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class PostDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val getPost: GetPostUseCase,
    private val observeBookmarkedIds: ObserveBookmarkedIdsUseCase,
    private val toggleBookmark: ToggleBookmarkUseCase,
) : BaseViewModel<PostDetailIntent, PostDetailViewState, PostDetailSingleEvent>() {

    private val postId: Int = savedStateHandle.toRoute<PostDetailScreenProvider.PostDetail>().postId

    override fun initState() = PostDetailViewState()

    init {
        load()
        viewModelScope.launch {
            observeBookmarkedIds()
                .map { ids -> postId in ids }
                .distinctUntilChanged()
                .collect { bookmarked -> updateState { copy(isBookmarked = bookmarked) } }
        }
    }

    override fun processIntent(intent: PostDetailIntent) {
        when (intent) {
            PostDetailIntent.BackClick -> sendEvent(PostDetailSingleEvent.NavigateBack)
            PostDetailIntent.Retry -> load()
            PostDetailIntent.BookmarkClick -> bookmark()
        }
    }

    private fun load() {
        updateState { copy(isLoading = true, error = null) }
        viewModelScope.launch {
            getPost(postId)
                .onSuccess { post -> updateState { copy(isLoading = false, post = post) } }
                .onFailure { error -> updateState { copy(isLoading = false, error = error.toUiError()) } }
        }
    }

    private fun bookmark() {
        val post = currentState.post ?: return
        val wasBookmarked = currentState.isBookmarked
        viewModelScope.launch {
            toggleBookmark(post)
                .onSuccess {
                    val message = if (wasBookmarked) R.string.bookmark_removed else R.string.bookmark_added
                    sendEvent(PostDetailSingleEvent.ShowToast(message, success = true))
                }
                .onFailure { error -> sendEvent(PostDetailSingleEvent.ShowToast(error.toErrorRes(), success = false)) }
        }
    }
}
