package com.base.app.feature.main.presentation.postdetail

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.base.app.core.designsystem.component.SkeletonBlock
import com.base.app.core.designsystem.component.SkeletonHost
import com.base.app.core.designsystem.component.rememberToastState
import com.base.app.core.designsystem.icon.AppIcons
import com.base.app.core.designsystem.theme.AppTheme
import com.base.app.core.designsystem.theme.AppTypography
import com.base.app.core.ui.foundation.ext.CollectAsEffect
import com.base.app.domain.model.Post
import com.base.app.feature.main.presentation.common.UiErrorState

@Composable
fun PostDetailScreen(
    viewModel: PostDetailViewModel,
    onBack: () -> Unit,
) {
    val viewState by viewModel.viewState.collectAsStateWithLifecycle()
    val toastState = rememberToastState()
    val resources = LocalResources.current

    viewModel.singleEvent.CollectAsEffect { event ->
        when (event) {
            PostDetailSingleEvent.NavigateBack -> onBack()
            is PostDetailSingleEvent.ShowToast -> toastState.show(resources.getString(event.messageRes), event.success)
        }
    }
    BackHandler { viewModel.processIntent(PostDetailIntent.BackClick) }

    Box(Modifier.fillMaxSize()) {
        PostDetailContent(
            viewState = viewState,
            onBackClick = { viewModel.processIntent(PostDetailIntent.BackClick) },
            onRetry = { viewModel.processIntent(PostDetailIntent.Retry) },
            onBookmarkClick = { viewModel.processIntent(PostDetailIntent.BookmarkClick) },
        )
        AppToastHost(
            state = toastState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = AppTheme.dimensions.s4),
        )
    }
}

@Composable
private fun PostDetailContent(
    viewState: PostDetailViewState,
    onBackClick: () -> Unit,
    onRetry: () -> Unit,
    onBookmarkClick: () -> Unit,
) {
    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        AppTopBar(
            title = stringResource(R.string.post_detail_title),
            onBack = onBackClick,
            backContentDescription = stringResource(R.string.common_back),
            actions = {
                if (viewState.post != null) {
                    IconButton(onClick = onBookmarkClick) {
                        Icon(
                            imageVector = if (viewState.isBookmarked) AppIcons.BookmarkFilled else AppIcons.Bookmark,
                            contentDescription = stringResource(
                                if (viewState.isBookmarked) R.string.post_unbookmark_cd else R.string.post_bookmark_cd,
                            ),
                            tint = if (viewState.isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground,
                        )
                    }
                }
            },
        )
        when {
            viewState.isLoading -> PostDetailSkeleton()
            viewState.error != null -> UiErrorState(error = viewState.error, onRetry = onRetry)
            viewState.post != null -> PostBody(viewState.post)
        }
    }
}

@Composable
private fun PostBody(post: Post) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
            .padding(horizontal = AppTheme.dimensions.screenGutter, vertical = AppTheme.dimensions.s3),
        verticalArrangement = Arrangement.spacedBy(AppTheme.dimensions.s3),
    ) {
        Text(post.title, style = AppTypography.headline, color = MaterialTheme.colorScheme.onBackground)
        if (post.tags.isNotEmpty()) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(AppTheme.dimensions.s2)) {
                post.tags.forEach { tag ->
                    Text(
                        text = "#$tag",
                        style = AppTypography.captionSemibold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
        Text(post.body, style = AppTypography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun PostDetailSkeleton() {
    SkeletonHost {
        Column(
            Modifier.padding(horizontal = AppTheme.dimensions.screenGutter, vertical = AppTheme.dimensions.s3),
            verticalArrangement = Arrangement.spacedBy(AppTheme.dimensions.s3),
        ) {
            SkeletonBlock(Modifier.fillMaxWidth().height(28.dp))
            repeat(5) { SkeletonBlock(Modifier.fillMaxWidth().height(16.dp)) }
        }
    }
}

@Preview(heightDp = 640)
@Composable
private fun PostDetailContentPreview() {
    AppTheme {
        PostDetailContent(
            viewState = PostDetailViewState(
                isLoading = false,
                post = Post(id = 1, title = "His mother had always taught him", body = "Body ".repeat(40), tags = listOf("history", "crime")),
                isBookmarked = true,
            ),
            onBackClick = {},
            onRetry = {},
            onBookmarkClick = {},
        )
    }
}
