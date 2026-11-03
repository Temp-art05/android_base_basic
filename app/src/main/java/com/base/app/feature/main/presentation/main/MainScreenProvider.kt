package com.base.app.feature.main.presentation.main

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import com.base.app.core.navigation.core.NavigationRequest
import com.base.app.core.navigation.core.ScreenProvider
import com.base.app.core.navigation.destination.Destination
import com.base.app.feature.main.presentation.language.LanguageScreenProvider
import com.base.app.feature.main.presentation.player.PlayerScreenProvider
import com.base.app.feature.main.presentation.postdetail.PostDetailScreenProvider
import kotlin.reflect.KType
import kotlinx.serialization.Serializable

class MainScreenProvider : ScreenProvider<MainScreenProvider.Main, MainViewModel>() {

    /** [initialTab] là tên một `MainTab` (null = Home) — cho lối vào cần mở thẳng một tab (deeplink, thông báo). */
    @Serializable
    data class Main(val initialTab: String? = null) : Destination

    override val typeMap: Map<KType, NavType<*>> = emptyMap()

    override fun invoke(
        viewModel: MainViewModel,
        onNavigationRequest: (NavigationRequest) -> Unit
    ): @Composable () -> Unit = {
        MainScreen(
            viewModel = viewModel,
            onOpenPostDetail = { postId ->
                onNavigationRequest(NavigationRequest.NavigateTo(PostDetailScreenProvider.PostDetail(postId)))
            },
            onOpenPlayer = { videoId ->
                onNavigationRequest(NavigationRequest.NavigateTo(PlayerScreenProvider.Player(videoId)))
            },
            onOpenLanguage = { onNavigationRequest(NavigationRequest.NavigateTo(LanguageScreenProvider.Language)) },
        )
    }
}
