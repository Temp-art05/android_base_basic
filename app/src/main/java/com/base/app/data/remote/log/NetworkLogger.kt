package com.base.app.data.remote.log

import io.ktor.client.plugins.logging.Logger
import timber.log.Timber

/** Log HTTP qua Timber, che token trước khi in ra logcat. */
object NetworkLogger : Logger {

    private val sensitivePatterns = listOf(
        Regex("(?i)(authorization:\\s*Bearer\\s+)([A-Za-z0-9._-]+)"),
        Regex("(?i)(\"refreshToken\"\\s*:\\s*\")([^\"]+)"),
        Regex("(?i)(\"accessToken\"\\s*:\\s*\")([^\"]+)"),
        Regex("(?i)(\"idToken\"\\s*:\\s*\")([^\"]+)"),
    )

    override fun log(message: String) {
        val redacted = sensitivePatterns.fold(message) { acc, regex ->
            regex.replace(acc) { "${it.groupValues[1]}***" }
        }
        Timber.tag("Network").d(redacted)
    }
}
