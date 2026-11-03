package com.base.app.core.navigation.destination

/**
 * Base interface for all navigation destinations.
 * Navigation 3 uses type-safe navigation with @Serializable data classes.
 *
 * Example - No args:
 * ```
 * @Serializable
 * data object Home : Destination
 * ```
 *
 * Example - With args:
 * ```
 * @Serializable
 * data class MediaPlayer(
 *     val id: String,
 *     val title: String
 * ) : Destination
 * ```
 */
interface Destination
