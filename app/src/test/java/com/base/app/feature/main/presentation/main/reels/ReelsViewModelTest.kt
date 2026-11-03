package com.base.app.feature.main.presentation.main.reels

import androidx.lifecycle.SavedStateHandle
import com.base.app.core.media.FakeFeedPlayer
import com.base.app.core.media.EpisodeFeedPlayerFactory
import com.base.app.core.media.PlaybackState
import com.base.app.domain.usecase.video.GetVideoFeedUseCase
import com.base.app.domain.usecase.video.PickVideoSourceUseCase
import com.base.app.testutil.FakeVideoRepository
import com.base.app.testutil.MainDispatcherRule
import com.base.app.testutil.video
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ReelsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val player = FakeFeedPlayer()
    private val repository = FakeVideoRepository(listOf(video("a"), video("b"), video("c")))

    private fun viewModel(handle: SavedStateHandle = SavedStateHandle()) = ReelsViewModel(
        savedStateHandle = handle,
        getVideoFeed = GetVideoFeedUseCase(repository),
        pickVideoSource = PickVideoSourceUseCase(),
        playerFactory = EpisodeFeedPlayerFactory { player },
    )

    private fun ReelsViewModel.show() {
        processIntent(ReelsIntent.TabVisible(true))
        processIntent(ReelsIntent.ScreenStarted)
    }

    @Test
    fun `loads feed into playlist without playing while hidden`() = runTest {
        val vm = viewModel()

        assertEquals(listOf("a", "b", "c"), vm.viewState.value.pages.map { it.videoId })
        assertEquals(listOf("a", "b", "c"), player.items.map { it.id })
        assertFalse(player.isActive)
        assertFalse(player.state.value.playWhenReady)
    }

    @Test
    fun `plays only when tab visible and screen started`() = runTest {
        val vm = viewModel()

        vm.processIntent(ReelsIntent.TabVisible(true))
        assertFalse(player.state.value.playWhenReady)

        vm.processIntent(ReelsIntent.ScreenStarted)
        assertTrue(player.isActive)
        assertTrue(player.state.value.playWhenReady)
    }

    @Test
    fun `hiding tab releases every decoder and coming back resumes`() = runTest {
        val vm = viewModel()
        vm.show()

        vm.processIntent(ReelsIntent.TabVisible(false))
        assertFalse(player.isActive)

        vm.processIntent(ReelsIntent.TabVisible(true))
        assertTrue(player.state.value.playWhenReady)
    }

    @Test
    fun `user pause survives leaving the tab`() = runTest {
        val vm = viewModel()
        vm.show()
        vm.processIntent(ReelsIntent.TogglePlay)

        vm.processIntent(ReelsIntent.TabVisible(false))
        vm.processIntent(ReelsIntent.TabVisible(true))

        assertFalse(player.state.value.playWhenReady)
    }

    @Test
    fun `rotation stop is ignored`() = runTest {
        val vm = viewModel()
        vm.show()

        vm.processIntent(ReelsIntent.ScreenStopped(isChangingConfigurations = true))

        assertTrue(player.isActive)
        assertTrue(player.state.value.playWhenReady)
    }

    @Test
    fun `swiping opens the new reel from zero and remembers it`() = runTest {
        val handle = SavedStateHandle()
        val vm = viewModel(handle)
        vm.show()

        vm.processIntent(ReelsIntent.PageSettled(2))

        assertEquals(2, vm.viewState.value.current)
        assertEquals(2, handle.get<Int>(ReelsViewModel.KEY_CURRENT))
        assertEquals(FakeFeedPlayer.Load("https://cdn/c/720.mp4", 0, true), player.loads.last())
    }

    @Test
    fun `restores reel after process death`() = runTest {
        val vm = viewModel(SavedStateHandle(mapOf(ReelsViewModel.KEY_CURRENT to 1)))

        assertEquals(1, vm.viewState.value.current)
        assertEquals(1, player.currents.last())
    }

    @Test
    fun `ended reel scrolls to the next one`() = runTest {
        val vm = viewModel()
        vm.show()
        val events = mutableListOf<ReelsSingleEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.singleEvent.toList(events) }

        player.emit(PlaybackState(itemId = "a", ended = true, durationMs = 10_000))

        assertEquals(listOf<ReelsSingleEvent>(ReelsSingleEvent.ScrollToPage(1)), events)
    }

    @Test
    fun `feed respects quality cap`() = runTest {
        repository.videos = listOf(video("a", "1080p" to "u1080", "480p" to "u480"))
        viewModel()

        assertEquals("u480", player.items.single().url)
    }

    @Test
    fun `cycle speed goes to player and saved state`() = runTest {
        val handle = SavedStateHandle()
        val vm = viewModel(handle)

        vm.processIntent(ReelsIntent.CycleSpeed)

        assertEquals(1.5f, player.lastSpeed)
        assertEquals(1.5f, handle.get<Float>(ReelsViewModel.KEY_SPEED))
    }
}
