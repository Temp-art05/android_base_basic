package com.base.app.feature.main.presentation.main.bookmarks

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.base.app.R
import com.base.app.core.designsystem.component.AppToastHost
import com.base.app.core.designsystem.component.AppTopBar
import com.base.app.core.designsystem.component.BlankState
import com.base.app.core.designsystem.component.rememberToastState
import com.base.app.core.designsystem.icon.AppIcons
import com.base.app.core.designsystem.theme.AppTheme
import com.base.app.core.ui.foundation.ext.CollectAsEffect
import com.base.app.domain.model.Post
import com.base.app.feature.main.presentation.common.PostItem
import org.koin.androidx.compose.koinViewModel

@Composable
internal fun BookmarksScreen(
    onOpenPostDetail: (postId: Int) -> Unit,
    onOpenHomeTab: () -> Unit,
    viewModel: BookmarksViewModel = koinViewModel(),
) {
    val viewState by viewModel.viewState.collectAsStateWithLifecycle()
    val toastState = rememberToastState()
    val resources = LocalResources.current

    viewModel.singleEvent.CollectAsEffect { event ->
        when (event) {
            is BookmarksSingleEvent.OpenPostDetail -> onOpenPostDetail(event.postId)
            BookmarksSingleEvent.OpenHomeTab -> onOpenHomeTab()
            is BookmarksSingleEvent.ShowToast -> toastState.show(resources.getString(event.messageRes), event.success)
        }
    }

    Box(Modifier.fillMaxSize()) {
        BookmarksContent(
            viewState = viewState,
            onPostClick = { postId -> viewModel.processIntent(BookmarksIntent.PostClick(postId)) },
            onRemoveClick = { post -> viewModel.processIntent(BookmarksIntent.RemoveClick(post)) },
            onBrowsePostsClick = { viewModel.processIntent(BookmarksIntent.BrowsePostsClick) },
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
private fun BookmarksContent(
    viewState: BookmarksViewState,
    onPostClick: (postId: Int) -> Unit,
    onRemoveClick: (Post) -> Unit,
    onBrowsePostsClick: () -> Unit,
) {
    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        AppTopBar(title = stringResource(R.string.bookmarks_title), onBack = null)
        when {
            viewState.isLoading -> Unit
            viewState.posts.isEmpty() -> BlankState(
                glyph = AppIcons.Bookmark,
                title = stringResource(R.string.bookmarks_empty_title),
                body = stringResource(R.string.bookmarks_empty_body),
                actionLabel = stringResource(R.string.bookmarks_empty_action),
                onAction = onBrowsePostsClick,
            )
            else -> {
                val gutter = AppTheme.dimensions.screenGutter
                LazyColumn(
                    contentPadding = PaddingValues(start = gutter, end = gutter, bottom = AppTheme.dimensions.s6),
                    verticalArrangement = Arrangement.spacedBy(AppTheme.dimensions.s3),
                ) {
                    items(viewState.posts, key = { it.id }) { post ->
                        PostItem(
                            post = post,
                            isBookmarked = true,
                            onClick = { onPostClick(post.id) },
                            onBookmarkClick = { onRemoveClick(post) },
                        )
                    }
                }
            }
        }
    }
}

@Preview(heightDp = 640)
@Composable
private fun BookmarksEmptyPreview() {
    AppTheme {
        BookmarksContent(
            viewState = BookmarksViewState(isLoading = false),
            onPostClick = {},
            onRemoveClick = {},
            onBrowsePostsClick = {},
        )
    }
}
