package com.base.app.feature.main.presentation.postdetail

import androidx.annotation.StringRes
import com.base.app.core.ui.foundation.error.UiError
import com.base.app.core.ui.foundation.vm.SingleEvent
import com.base.app.core.ui.foundation.vm.ViewIntent
import com.base.app.core.ui.foundation.vm.ViewState
import com.base.app.domain.model.Post

/** SAMPLE — màn là một destination có tham số (`postId`), đọc qua `SavedStateHandle.toRoute`. */
data class PostDetailViewState(
    val post: Post? = null,
    val isBookmarked: Boolean = false,
    val isLoading: Boolean = true,
    val error: UiError? = null,
) : ViewState

sealed interface PostDetailIntent : ViewIntent {
    data object BackClick : PostDetailIntent
    data object Retry : PostDetailIntent
    data object BookmarkClick : PostDetailIntent
}

sealed interface PostDetailSingleEvent : SingleEvent {
    data object NavigateBack : PostDetailSingleEvent
    data class ShowToast(@param:StringRes val messageRes: Int, val success: Boolean) : PostDetailSingleEvent
}
