package com.base.app.feature.main.presentation.player

import androidx.compose.runtime.Immutable
import com.base.app.core.media.PlaybackState
import com.base.app.core.ui.foundation.error.UiError
import com.base.app.core.ui.foundation.vm.SingleEvent
import com.base.app.core.ui.foundation.vm.ViewIntent
import com.base.app.core.ui.foundation.vm.ViewState

/** Một trang của pager dọc = một video. Ổn định cho Compose: trang không recompose khi state khác đổi. */
@Immutable
data class PlayerPage(
    val videoId: String,
    val title: String,
    val description: String,
    val thumbnailUrl: String?,
)

data class PlayerViewState(
    val isLoading: Boolean = true,
    val error: UiError? = null,
    val pages: List<PlayerPage> = emptyList(),
    /** Trang của video đang phát — VM là nguồn sự thật, pager chỉ báo `PageSettled` và cuộn theo `ScrollToPage`. */
    val current: Int = 0,
    val descriptionExpanded: Boolean = false,
    /**
     * Cờ phát đổi chậm. `positionMs`/`bufferedMs` luôn 0 ở đây: vị trí tick đi qua
     * [PlayerViewModel.playbackProgress] để không recompose cả màn.
     */
    val playback: PlaybackState = PlaybackState(),
    val speed: Float = 1f,
    val optionsVisible: Boolean = false,
    /** Quality có thật của video đang phát, cao → thấp. */
    val qualities: List<String> = emptyList(),
    /** Null = Auto. */
    val quality: String? = null,
    /** Auto đã tự hạ xuống quality này vì mạng chậm. */
    val qualityDropped: String? = null,
    /** Video không có nguồn phát được: báo trên trang đó, pager vẫn vuốt sang video khác. */
    val sourceUnavailable: Boolean = false,
    /** Hết video cuối của feed — hiện thẻ xem lại. */
    val feedEnded: Boolean = false,
) : ViewState {
    val hasPlaybackError: Boolean get() = playback.error != null
}

sealed interface PlayerIntent : ViewIntent {
    data object BackClick : PlayerIntent
    data object RetryClick : PlayerIntent
    data object TogglePlay : PlayerIntent
    data class SeekTo(val positionMs: Long) : PlayerIntent
    data object SpeedClick : PlayerIntent
    data object OptionsClick : PlayerIntent
    data object DismissOptions : PlayerIntent
    data class SpeedSelect(val speed: Float) : PlayerIntent

    /** Null = Auto. */
    data class QualitySelect(val quality: String?) : PlayerIntent
    data object SwitchBackToHigherQuality : PlayerIntent
    data object ToggleDescription : PlayerIntent
    data object ReplayClick : PlayerIntent

    /** Pager dọc đã dừng ở [page] — user vuốt. */
    data class PageSettled(val page: Int) : PlayerIntent

    /** Lifecycle `ON_START` / `ON_STOP` của màn. Xoay máy cũng qua STOP/START: bỏ qua để không khựng tiếng. */
    data object ScreenStarted : PlayerIntent
    data class ScreenStopped(val isChangingConfigurations: Boolean = false) : PlayerIntent
}

sealed interface PlayerSingleEvent : SingleEvent {
    data object NavigateBack : PlayerSingleEvent

    /** VM đổi video không qua vuốt (tự sang video kế) → pager cuộn tới [page]. */
    data class ScrollToPage(val page: Int) : PlayerSingleEvent
}
