package com.base.app.feature.main.presentation.main.reels

import androidx.compose.runtime.Immutable
import com.base.app.core.media.PlaybackState
import com.base.app.core.ui.foundation.error.UiError
import com.base.app.core.ui.foundation.vm.SingleEvent
import com.base.app.core.ui.foundation.vm.ViewIntent
import com.base.app.core.ui.foundation.vm.ViewState

/** Một trang Reels = một video. Đủ thông tin để overlay trượt cùng trang, kể cả trang lân cận chưa phát. */
@Immutable
data class ReelPage(
    val videoId: String,
    val title: String,
    val description: String,
    val thumbnailUrl: String?,
)

data class ReelsViewState(
    val isLoading: Boolean = true,
    val error: UiError? = null,
    val pages: List<ReelPage> = emptyList(),
    /** Vị trí trang đang phát — VM là nguồn sự thật, pager chỉ báo `PageSettled`. */
    val current: Int = 0,
    val speed: Float = 1f,
    val descriptionExpanded: Boolean = false,
    /** Cờ phát đổi chậm; tiến độ tick đi qua `ReelsViewModel.playbackProgress`. */
    val playback: PlaybackState = PlaybackState(),
) : ViewState

sealed interface ReelsIntent : ViewIntent {
    data class PageSettled(val page: Int) : ReelsIntent
    data object TogglePlay : ReelsIntent
    data object CycleSpeed : ReelsIntent
    data object ToggleDescription : ReelsIntent
    data object OpenPlayerClick : ReelsIntent
    data object Retry : ReelsIntent

    /** Thử phát lại reel hiện tại sau lỗi phát. */
    data object RetryPlayback : ReelsIntent

    /** Tab ẩn vẫn RESUMED (pager giữ tab) → gate phát theo tab đang chọn. */
    data class TabVisible(val visible: Boolean) : ReelsIntent
    data object ScreenStarted : ReelsIntent

    /** Xoay máy cũng qua STOP/START: bỏ qua để không khựng tiếng. */
    data class ScreenStopped(val isChangingConfigurations: Boolean = false) : ReelsIntent
}

sealed interface ReelsSingleEvent : SingleEvent {
    /** Reel hết → tự cuộn sang reel kế. */
    data class ScrollToPage(val page: Int) : ReelsSingleEvent
    data class OpenPlayer(val videoId: String) : ReelsSingleEvent
}
