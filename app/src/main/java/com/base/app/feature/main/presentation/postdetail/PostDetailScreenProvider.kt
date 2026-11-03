package com.base.app.feature.main.presentation.postdetail

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import com.base.app.core.navigation.core.NavigationRequest
import com.base.app.core.navigation.core.ScreenProvider
import com.base.app.core.navigation.destination.Destination
import kotlin.reflect.KType
import kotlinx.serialization.Serializable

/** SAMPLE — mở từ Home và Bookmarks. */
class PostDetailScreenProvider : ScreenProvider<PostDetailScreenProvider.PostDetail, PostDetailViewModel>() {

    @Serializable
    data class PostDetail(val postId: Int) : Destination

    override val typeMap: Map<KType, NavType<*>> = emptyMap()

    override fun invoke(
        viewModel: PostDetailViewModel,
        onNavigationRequest: (NavigationRequest) -> Unit
    ): @Composable () -> Unit = {
        PostDetailScreen(
            viewModel = viewModel,
            onBack = { onNavigationRequest(NavigationRequest.PopBackStack) },
        )
    }
}
