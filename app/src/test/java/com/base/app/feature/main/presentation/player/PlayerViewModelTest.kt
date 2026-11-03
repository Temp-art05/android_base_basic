package com.base.app.feature.main.presentation.player

import android.app.Application
import androidx.lifecycle.SavedStateHandle
import com.base.app.core.media.EpisodeFeedPlayerFactory
import com.base.app.core.media.FakeFeedPlayer
import com.base.app.core.media.PlaybackState
import com.base.app.domain.model.WatchProgress
import com.base.app.domain.usecase.progress.GetResumePositionUseCase
import com.base.app.domain.usecase.progress.SaveWatchProgressUseCase
import com.base.app.domain.usecase.video.GetVideoFeedUseCase
import com.base.app.domain.usecase.video.PickVideoSourceUseCase
import com.base.app.domain.usecase.video.PlaybackQualityPolicy
import com.base.app.testutil.FakeVideoRepository
import com.base.app.testutil.FakeWatchProgressRepository
import com.base.app.testutil.MainDispatcherRule
import com.base.app.testutil.video
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Robolectric: `SavedStateHandle.toRoute` đọc route qua `Bundle`. SDK 35: Robolectric cho SDK 37 cần JDK 21, toolchain là 17.
 * `Application` trống: `BaseApp` dựng Koin + SDK thật, test không cần. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class PlayerViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val player = FakeFeedPlayer()
    private val videos = FakeVideoRepository(
        listOf(video("a"), video("ladder", "480p" to "u480", "240p" to "u240"), video("c")),
    )
    private val progress = FakeWatchProgressRepository()

    private fun viewModel(videoId: String = "a", saved: Map<String, Any?> = emptyMap()) = PlayerViewModel(
        savedStateHandle = SavedStateHandle(mapOf("videoId" to videoId) + saved),
        getVideoFeed = GetVideoFeedUseCase(videos),
        pickVideoSource = PickVideoSourceUseCase(),
        getResumePosition = GetResumePositionUseCase(progress),
        saveWatchProgress = SaveWatchProgressUseCase(progress),
        playerFactory = EpisodeFeedPlayerFactory { player },
    )

    @Test
    fun `opens the route video at its saved position`() = runTest {
        progress.saved["ladder"] = WatchProgress("ladder", positionMs = 30_000, durationMs = 128_000, updatedAt = 0)

        val vm = viewModel(videoId = "ladder")

        assertEquals(1, vm.viewState.value.current)
        assertEquals(FakeFeedPlayer.Load("u480", 30_000, true), player.loads.single())
        assertEquals(listOf("480p", "240p"), vm.viewState.value.qualities)
    }

    @Test
    fun `saved video wins over route after process death`() = runTest {
        val vm = viewModel(videoId = "a", saved = mapOf(PlayerViewModel.KEY_VIDEO_ID to "c"))

        assertEquals(2, vm.viewState.value.current)
    }

    @Test
    fun `background pauses, drops preload and saves position`() = runTest {
        val vm = viewModel()
        player.playing(positionMs = 12_000, durationMs = 100_000)

        vm.processIntent(PlayerIntent.ScreenStopped())

        assertFalse(player.state.value.playWhenReady)
        assertFalse(player.isPreloadActive)
        assertEquals(12_000L, progress.saved["a"]?.positionMs)

        vm.processIntent(PlayerIntent.ScreenStarted)
        assertTrue(player.isPreloadActive)
        assertTrue(player.state.value.playWhenReady)
    }

    @Test
    fun `swiping saves old video and resumes new one`() = runTest {
        progress.saved["c"] = WatchProgress("c", positionMs = 7_000, durationMs = 50_000, updatedAt = 0)
        val vm = viewModel()
        player.playing(positionMs = 20_000)

        vm.processIntent(PlayerIntent.PageSettled(2))

        assertEquals(20_000L, progress.saved["a"]?.positionMs)
        assertEquals(FakeFeedPlayer.Load("https://cdn/c/720.mp4", 7_000, true), player.loads.last())
    }

    @Test
    fun `choosing quality reloads at the current position`() = runTest {
        val vm = viewModel(videoId = "ladder")
        player.playing(positionMs = 40_000)

        vm.processIntent(PlayerIntent.QualitySelect("240p"))

        assertEquals(FakeFeedPlayer.Load("u240", 40_000, true), player.loads.last())
        assertEquals("240p", vm.viewState.value.quality)
    }

    @Test
    fun `auto drops quality after a long stall and can switch back`() = runTest(StandardTestDispatcher(mainDispatcherRule.dispatcher.scheduler)) {
        val vm = viewModel(videoId = "ladder")
        runCurrent()
        player.emit(PlaybackState(itemId = "ladder", isBuffering = true, playWhenReady = true, positionMs = 5_000, durationMs = 128_000))

        advanceTimeBy(PlaybackQualityPolicy.STALL_THRESHOLD_MS + 200)

        assertEquals("240p", vm.viewState.value.qualityDropped)
        assertEquals("u240", player.loads.last().url)

        vm.processIntent(PlayerIntent.SwitchBackToHigherQuality)
        assertNull(vm.viewState.value.qualityDropped)
        assertEquals("u480", player.loads.last().url)
    }

    @Test
    fun `manual quality never auto drops`() = runTest(StandardTestDispatcher(mainDispatcherRule.dispatcher.scheduler)) {
        val vm = viewModel(videoId = "ladder", saved = mapOf(PlayerViewModel.KEY_QUALITY to "480p"))
        runCurrent()
        player.emit(PlaybackState(itemId = "ladder", isBuffering = true, playWhenReady = true, durationMs = 128_000))

        advanceTimeBy(PlaybackQualityPolicy.STALL_THRESHOLD_MS * 3)

        assertNull(vm.viewState.value.qualityDropped)
    }

    @Test
    fun `end of a video scrolls to the next, end of feed shows replay`() = runTest {
        val vm = viewModel(videoId = "a")
        val events = mutableListOf<PlayerSingleEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.singleEvent.toList(events) }

        player.emit(PlaybackState(itemId = "a", ended = true, durationMs = 10_000))
        assertEquals(listOf<PlayerSingleEvent>(PlayerSingleEvent.ScrollToPage(1)), events)

        vm.processIntent(PlayerIntent.PageSettled(2))
        player.emit(PlaybackState(itemId = "c", ended = true, durationMs = 10_000))
        assertTrue(vm.viewState.value.feedEnded)
    }

    @Test
    fun `back saves position then navigates`() = runTest {
        val vm = viewModel()
        val events = mutableListOf<PlayerSingleEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.singleEvent.toList(events) }
        player.playing(positionMs = 9_000)

        vm.processIntent(PlayerIntent.BackClick)

        assertEquals(9_000L, progress.saved["a"]?.positionMs)
        assertEquals(listOf<PlayerSingleEvent>(PlayerSingleEvent.NavigateBack), events)
    }
}
