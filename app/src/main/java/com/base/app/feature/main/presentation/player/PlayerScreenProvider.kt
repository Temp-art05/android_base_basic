package com.base.app.feature.main.presentation.player

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import com.base.app.core.navigation.core.NavigationRequest
import com.base.app.core.navigation.core.ScreenProvider
import com.base.app.core.navigation.destination.Destination
import kotlin.reflect.KType
import kotlinx.serialization.Serializable

class PlayerScreenProvider : ScreenProvider<PlayerScreenProvider.Player, PlayerViewModel>() {

    /** Mở Player ở video [videoId]; vuốt dọc sang các video khác của feed. */
    @Serializable
    data class Player(val videoId: String) : Destination

    override val typeMap: Map<KType, NavType<*>> = emptyMap()

    override fun invoke(
        viewModel: PlayerViewModel,
        onNavigationRequest: (NavigationRequest) -> Unit
    ): @Composable () -> Unit = {
        PlayerScreen(
            viewModel = viewModel,
            onBack = { onNavigationRequest(NavigationRequest.PopBackStack) },
        )
    }
}
