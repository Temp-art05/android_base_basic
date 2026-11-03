package com.base.app.feature.main.presentation.main.home

import com.base.app.core.utils.error.DomainException
import com.base.app.domain.usecase.bookmark.ObserveBookmarkedIdsUseCase
import com.base.app.domain.usecase.bookmark.ToggleBookmarkUseCase
import com.base.app.domain.usecase.post.GetPostsPageUseCase
import com.base.app.testutil.FakeBookmarkRepository
import com.base.app.testutil.FakePostRepository
import com.base.app.testutil.MainDispatcherRule
import com.base.app.testutil.post
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val postRepository = FakePostRepository(total = 30)
    private val bookmarkRepository = FakeBookmarkRepository()

    private fun viewModel() = HomeViewModel(
        getPostsPage = GetPostsPageUseCase(postRepository),
        observeBookmarkedIds = ObserveBookmarkedIdsUseCase(bookmarkRepository),
        toggleBookmark = ToggleBookmarkUseCase(bookmarkRepository),
    )

    @Test
    fun `loads first page on init`() = runTest {
        val state = viewModel().viewState.value

        assertFalse(state.isLoading)
        assertEquals(GetPostsPageUseCase.PAGE_SIZE, state.posts.size)
        assertTrue(state.hasMore)
    }

    @Test
    fun `load more appends until last page`() = runTest {
        val vm = viewModel()

        vm.processIntent(HomeIntent.LoadMore)
        vm.processIntent(HomeIntent.LoadMore) // hết trang → bỏ qua

        assertEquals(30, vm.viewState.value.posts.size)
        assertFalse(vm.viewState.value.hasMore)
        assertEquals(2, postRepository.requests.size)
    }

    @Test
    fun `first page failure shows error and retry recovers`() = runTest {
        postRepository.failure = DomainException.Network(RuntimeException())
        val vm = viewModel()
        assertNotNull(vm.viewState.value.error)

        postRepository.failure = null
        vm.processIntent(HomeIntent.Retry)

        assertEquals(null, vm.viewState.value.error)
        assertEquals(GetPostsPageUseCase.PAGE_SIZE, vm.viewState.value.posts.size)
    }

    @Test
    fun `bookmark click updates bookmarked ids from repository`() = runTest {
        val vm = viewModel()

        vm.processIntent(HomeIntent.BookmarkClick(post(3)))

        assertEquals(setOf(3), vm.viewState.value.bookmarkedIds)
    }
}
