package com.base.app.feature.main.presentation.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.base.app.R
import com.base.app.core.designsystem.icon.AppIcons
import com.base.app.core.designsystem.theme.AppTheme
import com.base.app.core.designsystem.theme.AppTypography
import com.base.app.domain.model.Post

/** SAMPLE — thẻ một post: tiêu đề, 2 dòng nội dung, nút bookmark. Dùng chung cho Home và Bookmarks. */
@Composable
internal fun PostItem(
    post: Post,
    isBookmarked: Boolean,
    onClick: () -> Unit,
    onBookmarkClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = AppTheme.shapes.card
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, shape)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(start = AppTheme.dimensions.s4, top = AppTheme.dimensions.s3, bottom = AppTheme.dimensions.s3),
        verticalAlignment = Alignment.Top,
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = post.title,
                style = AppTypography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = post.body,
                style = AppTypography.body,
                color = AppTheme.colors.text3,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = AppTheme.dimensions.s1),
            )
        }
        IconButton(onClick = onBookmarkClick) {
            Icon(
                imageVector = if (isBookmarked) AppIcons.BookmarkFilled else AppIcons.Bookmark,
                contentDescription = stringResource(if (isBookmarked) R.string.post_unbookmark_cd else R.string.post_bookmark_cd),
                tint = if (isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Preview
@Composable
private fun PostItemPreview() {
    AppTheme {
        PostItem(
            post = Post(id = 1, title = "His mother had always taught him", body = "His mother had always taught him not to ever think of himself as better than others."),
            isBookmarked = true,
            onClick = {},
            onBookmarkClick = {},
        )
    }
}
