package com.base.app.feature.main.presentation.main.bookmarks

import androidx.annotation.StringRes
import com.base.app.core.ui.foundation.vm.SingleEvent
import com.base.app.core.ui.foundation.vm.ViewIntent
import com.base.app.core.ui.foundation.vm.ViewState
import com.base.app.domain.model.Post

/** SAMPLE — danh sách bookmark stream từ Room: bookmark ở Home/Detail là tab này tự cập nhật. */
data class BookmarksViewState(
    val posts: List<Post> = emptyList(),
    val isLoading: Boolean = true,
) : ViewState

sealed interface BookmarksIntent : ViewIntent {
    data class PostClick(val postId: Int) : BookmarksIntent
    data class RemoveClick(val post: Post) : BookmarksIntent
    data object BrowsePostsClick : BookmarksIntent
}

sealed interface BookmarksSingleEvent : SingleEvent {
    data class OpenPostDetail(val postId: Int) : BookmarksSingleEvent
    data object OpenHomeTab : BookmarksSingleEvent
    data class ShowToast(@param:StringRes val messageRes: Int, val success: Boolean) : BookmarksSingleEvent
}
