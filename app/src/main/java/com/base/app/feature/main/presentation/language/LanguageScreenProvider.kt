package com.base.app.feature.main.presentation.language

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import com.base.app.core.navigation.core.NavigationRequest
import com.base.app.core.navigation.core.ScreenProvider
import com.base.app.core.navigation.destination.Destination
import kotlin.reflect.KType
import kotlinx.serialization.Serializable

/** Chọn ngôn ngữ giao diện, mở từ tab Settings. */
class LanguageScreenProvider : ScreenProvider<LanguageScreenProvider.Language, LanguageViewModel>() {

    @Serializable
    data object Language : Destination

    override val typeMap: Map<KType, NavType<*>> = emptyMap()

    override fun invoke(
        viewModel: LanguageViewModel,
        onNavigationRequest: (NavigationRequest) -> Unit
    ): @Composable () -> Unit = {
        LanguageScreen(
            viewModel = viewModel,
            onBack = { onNavigationRequest(NavigationRequest.NavigateUp()) },
        )
    }
}
