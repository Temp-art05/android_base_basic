package com.base.app.data.remote.http

import org.koin.core.qualifier.Qualifier
import org.koin.core.qualifier.named

/**
 * Qualifier cho khi cần nhiều HttpClient (mỗi BE một client).
 *
 * ```
 * val UploadClient = httpClientQualifier("UploadClient")
 * single(UploadClient) { provideHttpClient(...) }
 * single { UploadRemoteDataSource(get(UploadClient)) }
 * ```
 */
fun httpClientQualifier(name: String): Qualifier = named(name)
