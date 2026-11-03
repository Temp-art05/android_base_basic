package com.base.app.data.remote.http

import com.base.app.data.remote.dto.ApiErrorBody
import com.base.app.data.remote.error.ApiException
import com.base.app.data.remote.log.NetworkLogger
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

val defaultJson = Json {
    ignoreUnknownKeys = true
    isLenient = true
    encodeDefaults = true
    explicitNulls = false
    // `"field": null` vào field non-null → lấy default của DTO thay vì ném.
    coerceInputValues = true
}

/** [engine] để test thay bằng `MockEngine`; app dùng mặc định OkHttp. */
fun provideHttpClient(
    apiConfig: ApiConfig,
    enableLogging: Boolean = true,
    engine: HttpClientEngine = OkHttp.create(),
): HttpClient = HttpClient(engine) {
    install(ContentNegotiation) {
        json(defaultJson)
    }

    install(HttpTimeout) {
        requestTimeoutMillis = apiConfig.requestTimeout
        connectTimeoutMillis = apiConfig.connectTimeout
        socketTimeoutMillis = apiConfig.socketTimeout
    }

    install(DefaultRequest) {
        // Thiếu "/" cuối thì Ktor bỏ segment cuối của base url khi ghép path tương đối.
        url(apiConfig.baseUrl.ensureTrailingSlash())
        contentType(ContentType.Application.Json)
    }

    HttpResponseValidator {
        validateResponse { response ->
            if (!response.status.isSuccess()) throw response.toApiException()
        }
    }

    if (enableLogging) {
        install(Logging) {
            logger = NetworkLogger
            level = LogLevel.BODY
        }
    }
}

private suspend fun HttpResponse.toApiException(): ApiException {
    val body = runCatching { bodyAsText() }.getOrNull()
        ?.takeIf { it.isNotBlank() }
        ?.let { raw -> runCatching { defaultJson.decodeFromString<ApiErrorBody>(raw) }.getOrNull() }
    return ApiException.from(status.value, body)
}

private fun String.ensureTrailingSlash(): String = if (endsWith("/")) this else "$this/"
