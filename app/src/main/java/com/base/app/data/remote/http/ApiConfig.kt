package com.base.app.data.remote.http

import kotlin.time.Duration.Companion.seconds

data class ApiConfig(
    val baseUrl: String,
    val requestTimeout: Long = 30.seconds.inWholeMilliseconds,
    val connectTimeout: Long = 15.seconds.inWholeMilliseconds,
    val socketTimeout: Long = 30.seconds.inWholeMilliseconds,
)
