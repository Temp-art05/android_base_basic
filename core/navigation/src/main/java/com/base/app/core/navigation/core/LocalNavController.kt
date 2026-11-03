package com.base.app.core.navigation.core

import androidx.compose.runtime.compositionLocalOf
import androidx.navigation.NavHostController

/**
 * CompositionLocal for providing NavHostController throughout the composition tree.
 *
 * Usage:
 * ```kotlin
 * CompositionLocalProvider(LocalNavController provides navController) {
 *     NavHost(...) { ... }
 * }
 * ```
 *
 * Access:
 * ```kotlin
 * val navController = LocalNavController.current
 * ```
 */
val LocalNavController = compositionLocalOf<NavHostController> {
    error("No NavController provided! Make sure to wrap your NavHost with CompositionLocalProvider(LocalNavController provides navController)")
}
