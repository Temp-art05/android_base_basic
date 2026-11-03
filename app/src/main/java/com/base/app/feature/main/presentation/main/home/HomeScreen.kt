package com.base.app.feature.main.presentation.main.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.base.app.R
import com.base.app.core.designsystem.component.AppToastHost
import com.base.app.core.designsystem.component.AppTopBar
import com.base.app.core.designsystem.component.OutlinePillButton
import com.base.app.core.designsystem.component.SkeletonBlock
import com.base.app.core.designsystem.component.SkeletonHost
import com.base.app.core.designsystem.component.rememberToastState
import com.base.app.core.designsystem.theme.AppTheme
import com.base.app.core.ui.foundation.error.retryLabel
import com.base.app.core.ui.foundation.ext.CollectAsEffect
import com.base.app.domain.model.Post
import com.base.app.feature.main.presentation.common.PostItem
import com.base.app.feature.main.presentation.common.UiErrorState
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import org.koin.androidx.compose.koinViewModel

/** Còn ≤ ngần này item tới cuối list thì xin trang kế. */
private const val LOAD_MORE_THRESHOLD = 5

@Composable
internal fun HomeScreen(
    onOpenPostDetail: (postId: Int) -> Unit,
    viewModel: HomeViewModel = koinViewModel(),
) {
    val viewState by viewModel.viewState.collectAsStateWithLifecycle()
    val toastState = rememberToastState()
    val resources = LocalResources.current

    viewModel.singleEvent.CollectAsEffect { event ->
        when (event) {
            is HomeSingleEvent.OpenPostDetail -> onOpenPostDetail(event.postId)
            is HomeSingleEvent.ShowToast -> toastState.show(resources.getString(event.messageRes), event.success)
        }
    }

    Box(Modifier.fillMaxSize()) {
        HomeContent(
            viewState = viewState,
            onRetry = { viewModel.processIntent(HomeIntent.Retry) },
            onLoadMore = { viewModel.processIntent(HomeIntent.LoadMore) },
            onPostClick = { postId -> viewModel.processIntent(HomeIntent.PostClick(postId)) },
            onBookmarkClick = { post -> viewModel.processIntent(HomeIntent.BookmarkClick(post)) },
        )
        AppToastHost(
            state = toastState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = AppTheme.dimensions.s4),
        )
    }
}

@Composable
private fun HomeContent(
    viewState: HomeViewState,
    onRetry: () -> Unit,
    onLoadMore: () -> Unit,
    onPostClick: (postId: Int) -> Unit,
    onBookmarkClick: (Post) -> Unit,
) {
    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        AppTopBar(title = stringResource(R.string.home_title), onBack = null)
        when {
            viewState.isLoading -> HomeSkeleton()
            viewState.error != null -> UiErrorState(error = viewState.error, onRetry = onRetry)
            else -> PostList(
                viewState = viewState,
                onLoadMore = onLoadMore,
                onPostClick = onPostClick,
                onBookmarkClick = onBookmarkClick,
            )
        }
    }
}

@Composable
private fun PostList(
    viewState: HomeViewState,
    onLoadMore: () -> Unit,
    onPostClick: (postId: Int) -> Unit,
    onBookmarkClick: (Post) -> Unit,
) {
    val listState = rememberLazyListState()
    LoadMoreEffect(listState = listState, onLoadMore = onLoadMore)
    val gutter = AppTheme.dimensions.screenGutter
    LazyColumn(
        state = listState,
        contentPadding = PaddingValues(start = gutter, end = gutter, bottom = AppTheme.dimensions.s6),
        verticalArrangement = Arrangement.spacedBy(AppTheme.dimensions.s3),
    ) {
        items(viewState.posts, key = { it.id }) { post ->
            PostItem(
                post = post,
                isBookmarked = post.id in viewState.bookmarkedIds,
                onClick = { onPostClick(post.id) },
                onBookmarkClick = { onBookmarkClick(post) },
            )
        }
        when {
            viewState.isLoadingMore -> item(key = "loading-more") {
                Box(Modifier.fillMaxWidth().padding(AppTheme.dimensions.s4), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            viewState.loadMoreFailed -> item(key = "load-more-failed") {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    OutlinePillButton(text = retryLabel(), onClick = onLoadMore)
                }
            }
        }
    }
}

/** Cuộn gần cuối thì báo [onLoadMore]; VM tự chặn khi đang tải / hết trang / vừa lỗi. */
@Composable
private fun LoadMoreEffect(listState: LazyListState, onLoadMore: () -> Unit) {
    val currentOnLoadMore by rememberUpdatedState(onLoadMore)
    LaunchedEffect(listState) {
        snapshotFlow {
            val info = listState.layoutInfo
            val last = info.visibleItemsInfo.lastOrNull()?.index ?: 0
            info.totalItemsCount > 0 && last >= info.totalItemsCount - LOAD_MORE_THRESHOLD
        }
            .distinctUntilChanged()
            .filter { nearEnd -> nearEnd }
            .collect { currentOnLoadMore() }
    }
}

@Composable
private fun HomeSkeleton() {
    SkeletonHost {
        Column(
            Modifier.padding(horizontal = AppTheme.dimensions.screenGutter),
            verticalArrangement = Arrangement.spacedBy(AppTheme.dimensions.s3),
        ) {
            repeat(6) {
                SkeletonBlock(Modifier.fillMaxWidth().height(84.dp), shape = AppTheme.shapes.card)
            }
        }
    }
}

@Preview(heightDp = 640)
@Composable
private fun HomeContentPreview() {
    AppTheme {
        HomeContent(
            viewState = HomeViewState(
                isLoading = false,
                posts = List(4) { Post(id = it, title = "Post title $it", body = "Body of post $it, long enough to wrap onto two lines.") },
                bookmarkedIds = setOf(1),
            ),
            onRetry = {},
            onLoadMore = {},
            onPostClick = {},
            onBookmarkClick = {},
        )
    }
}
