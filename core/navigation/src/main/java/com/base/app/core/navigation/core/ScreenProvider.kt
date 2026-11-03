package com.base.app.core.navigation.core

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.navigation.NavType
import com.base.app.core.navigation.destination.Destination
import kotlin.reflect.KType

/**
 * Abstract provider for creating screen composables with their ViewModels.
 *
 * Each screen should have its own ScreenProvider implementation that:
 * 1. Extends this class with the appropriate Destination and ViewModel types
 * 2. Provides the typeMap for any custom NavTypes needed
 * 3. Implements the invoke function to create the screen composable
 *
 * Example:
 * ```kotlin
 * class HomeScreenProvider : ScreenProvider<Home, HomeViewModel>() {
 *     override val typeMap: Map<KType, NavType<*>> = emptyMap()
 *
 *     override fun invoke(
 *         viewModel: HomeViewModel,
 *         onNavigationRequest: (NavigationRequest) -> Unit
 *     ): @Composable () -> Unit = {
 *         HomeScreen(
 *             viewModel = viewModel,
 *             onNavigate = onNavigationRequest
 *         )
 *     }
 * }
 * ```
 *
 * Usage in NavGraph:
 * ```kotlin
 * NavHost(...) {
 *     applyDestination(HomeScreenProvider())
 *     applyDestination(DetailScreenProvider())
 * }
 * ```
 *
 * @param T The destination type this provider handles
 * @param VM The ViewModel type for this screen
 */
abstract class ScreenProvider<T : Destination, VM : ViewModel> {

    /**
     * Map of KType to NavType for custom argument types.
     * Override this if your destination has complex argument types.
     *
     * For most cases with simple types (String, Int, etc.), return emptyMap().
     */
    abstract val typeMap: Map<KType, NavType<*>>

    /**
     * Creates the screen composable with the given ViewModel and navigation callback.
     *
     * @param viewModel The ViewModel instance (injected via Koin)
     * @param onNavigationRequest Callback to handle navigation requests from the screen
     * @return A composable function that renders the screen
     */
    abstract operator fun invoke(
        viewModel: VM,
        onNavigationRequest: (NavigationRequest) -> Unit
    ): @Composable () -> Unit
}
