package com.base.app.feature.main.presentation.main.home

import androidx.annotation.StringRes
import com.base.app.core.ui.foundation.error.UiError
import com.base.app.core.ui.foundation.vm.SingleEvent
import com.base.app.core.ui.foundation.vm.ViewIntent
import com.base.app.core.ui.foundation.vm.ViewState
import com.base.app.domain.model.Post

/**
 * SAMPLE — feed post phân trang.
 * @param error lỗi của lần tải **đầu** (thay cả danh sách); lỗi tải thêm chỉ là [loadMoreFailed] ở cuối list.
 */
data class HomeViewState(
    val posts: List<Post> = emptyList(),
    val bookmarkedIds: Set<Int> = emptySet(),
    val isLoading: Boolean = true,
    val isLoadingMore: Boolean = false,
    val hasMore: Boolean = false,
    val loadMoreFailed: Boolean = false,
    val error: UiError? = null,
) : ViewState

sealed interface HomeIntent : ViewIntent {
    data object Retry : HomeIntent
    data object LoadMore : HomeIntent
    data class PostClick(val postId: Int) : HomeIntent
    data class BookmarkClick(val post: Post) : HomeIntent
}

sealed interface HomeSingleEvent : SingleEvent {
    data class OpenPostDetail(val postId: Int) : HomeSingleEvent
    data class ShowToast(@param:StringRes val messageRes: Int, val success: Boolean) : HomeSingleEvent
}
