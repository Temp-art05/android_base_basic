package com.base.app.core.navigation.core

import com.base.app.core.navigation.destination.Destination

/**
 * Sealed interface representing different navigation actions.
 *
 * Usage in Screen:
 * ```kotlin
 * when (event) {
 *     is SingleEvent.NavigateToDetail -> {
 *         onNavigate(NavigationRequest.NavigateTo(
 *             destination = MediaPlayerScreenProvider.MediaPlayer(argsKey = event.argsKey)
 *         ))
 *     }
 *     is SingleEvent.NavigateBack -> {
 *         onNavigate(NavigationRequest.PopBackStack())
 *     }
 * }
 * ```
 */
sealed interface NavigationRequest {

    /**
     * Navigate to a destination.
     *
     * @param destination The destination to navigate to
     * @param popCurrent If true, removes the current screen from backstack after navigation
     */
    data class NavigateTo(
        val destination: Destination,
        val popCurrent: Boolean = false
    ) : NavigationRequest

    /**
     * Navigate to a destination and clear the entire backstack.
     * Useful for login/logout flows.
     *
     * @param destination The destination to navigate to
     */
    data class NavigateToAndClearBackStack(
        val destination: Destination
    ) : NavigationRequest

    /**
     * Pop the backstack (go back).
     */
    data object PopBackStack : NavigationRequest

    /**
     * Navigate up in the navigation hierarchy.
     * Similar to pressing the back button, but respects the navigation hierarchy.
     *
     * @param fallBackDestination Destination to navigate to if there's no parent in the hierarchy
     */
    data class NavigateUp(
        val fallBackDestination: Destination? = null
    ) : NavigationRequest

    /**
     * Pop the backstack until the given destination is reached.
     * If the destination is not found in the backstack, navigate to the fallback
     * destination and clear the entire backstack.
     *
     * @param destination The destination to pop back to
     * @param fallBackDestination Destination to navigate to if [destination] is not in the backstack
     * @param inclusive If true, [destination] itself is also popped
     */
    data class PopTo(
        val destination: Destination,
        val fallBackDestination: Destination? = null,
        val inclusive: Boolean = false
    ) : NavigationRequest
}
