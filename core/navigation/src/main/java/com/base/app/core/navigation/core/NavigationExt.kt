package com.base.app.core.navigation.core

import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.base.app.core.navigation.destination.Destination
import org.koin.androidx.compose.koinViewModel
import timber.log.Timber

/**
 * Extension to apply a ScreenProvider to the NavGraphBuilder.
 *
 * This is the primary way to register screens in the navigation graph.
 * It handles ViewModel injection via Koin and navigation request handling with throttle.
 *
 * Usage:
 * ```kotlin
 * NavHost(navController = navController, startDestination = Home) {
 *     applyDestination(HomeScreenProvider())
 *     applyDestination(DetailScreenProvider())
 * }
 * ```
 *
 * @param screenProvider The ScreenProvider for this destination
 */
inline fun <reified T : Destination, reified VM : ViewModel> NavGraphBuilder.applyDestination(
    screenProvider: ScreenProvider<T, VM>
) {
    composable<T>(typeMap = screenProvider.typeMap) {
        val navController = LocalNavController.current
        val viewModel = koinViewModel<VM>()

        screenProvider.invoke(viewModel) { request ->
            // Apply throttle to prevent double navigation
            if (!NavigationThrottle.shouldNavigate()) {
                Timber.d("Navigation: Throttled navigation request")
                return@invoke
            }

            handleNavigationRequest(navController, request)
        }.invoke()
    }
}

/**
 * Handle a NavigationRequest using the NavController.
 *
 * @param navController The NavController to use for navigation
 * @param request The NavigationRequest to handle
 */
fun handleNavigationRequest(navController: NavController, request: NavigationRequest) {
    when (request) {
        is NavigationRequest.NavigateTo -> {
            Timber.d("Navigation: NavigateTo ${request.destination}")
            if (request.popCurrent) {
                val currentRoute = navController.currentBackStackEntry?.destination?.route
                navController.navigate(request.destination) {
                    currentRoute?.let { route ->
                        popUpTo(route) { inclusive = true }
                    }
                }
            } else {
                navController.navigate(request.destination)
            }
        }

        is NavigationRequest.NavigateToAndClearBackStack -> {
            Timber.d("Navigation: NavigateToAndClearBackStack ${request.destination}")
            navController.navigate(request.destination) {
                popUpTo(0) { inclusive = true }
            }
        }

        NavigationRequest.PopBackStack -> {
            Timber.d("Navigation: PopBackStack")
            navController.popBackStack()
        }

        is NavigationRequest.NavigateUp -> {
            Timber.d("Navigation: NavigateUp fallback=${request.fallBackDestination}")
            val navigated = navController.navigateUp()
            if (!navigated && request.fallBackDestination != null) {
                navController.navigate(request.fallBackDestination) {
                    popUpTo(0) { inclusive = true }
                }
            }
        }

        is NavigationRequest.PopTo -> {
            Timber.d(
                "Navigation: PopTo ${request.destination} " +
                    "inclusive=${request.inclusive} fallback=${request.fallBackDestination}"
            )
            val popped = navController.popBackStack(
                route = request.destination,
                inclusive = request.inclusive
            )
            if (!popped && request.fallBackDestination != null) {
                navController.navigate(request.fallBackDestination) {
                    popUpTo(0) { inclusive = true }
                }
            }
        }
    }
}
