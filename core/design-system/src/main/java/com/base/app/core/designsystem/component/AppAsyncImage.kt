package com.base.app.core.designsystem.component

import android.content.Context
import android.os.Build
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil3.ImageLoader
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.SubcomposeAsyncImage
import coil3.disk.DiskCache
import coil3.gif.AnimatedImageDecoder
import coil3.gif.GifDecoder
import coil3.memory.MemoryCache
import coil3.network.CacheStrategy
import coil3.network.ConnectivityChecker
import coil3.network.NetworkRequest
import coil3.network.NetworkResponse
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import coil3.request.Options
import okio.Path.Companion.toOkioPath

/**
 * Điểm vào duy nhất để load ảnh từ URL/Uri — màn hình không gọi thẳng Coil.
 * [placeholder] dùng chung cho cả 3 trạng thái: không có model, đang tải, lỗi.
 */
@Composable
fun AppAsyncImage(
    model: Any?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    placeholder: @Composable (BoxScope.() -> Unit)? = null,
) {
    val hasModel = when (model) {
        null -> false
        is String -> model.isNotBlank()
        else -> true
    }

    if (!hasModel) {
        // Căn giữa cho khớp slot loading/error của SubcomposeAsyncImage để placeholder không nhảy vị trí.
        Box(modifier, contentAlignment = Alignment.Center) { placeholder?.invoke(this) }
        return
    }

    SubcomposeAsyncImage(
        model = model,
        contentDescription = contentDescription,
        contentScale = contentScale,
        modifier = modifier,
        loading = { placeholder?.invoke(this) },
        error = { placeholder?.invoke(this) },
    )
}

/** ImageLoader cho cả app — `BaseApp.newImageLoader` trả hàm này. */
@OptIn(ExperimentalCoilApi::class)
fun appImageLoader(context: Context): ImageLoader = ImageLoader.Builder(context)
    .components {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            add(AnimatedImageDecoder.Factory())
        } else {
            add(GifDecoder.Factory())
        }
        // Phải tự khai network fetcher (không để ServiceLoader nạp) mới truyền được [ExpiringCacheStrategy].
        add(
            OkHttpNetworkFetcherFactory(
                cacheStrategy = {
                    // ConnectivityChecker của Coil tự kiểm quyền ACCESS_NETWORK_STATE nên không ném SecurityException.
                    ExpiringCacheStrategy(ConnectivityChecker(context))
                },
            ),
        )
    }
    .memoryCache {
        // Theo RAM máy thay vì số cố định: máy yếu không bị OOM, máy khoẻ không phí cache.
        MemoryCache.Builder()
            .maxSizePercent(context, MEMORY_CACHE_PERCENT)
            .build()
    }
    .diskCache {
        DiskCache.Builder()
            .directory(context.cacheDir.resolve(DISK_CACHE_DIR).toOkioPath())
            .maxSizeBytes(MAX_DISK_CACHE_BYTES)
            .build()
    }
    .build()

private const val MEMORY_CACHE_PERCENT = 0.25

private const val MAX_DISK_CACHE_BYTES = 100L * 1024 * 1024

private const val MAX_AGE_MILLIS = 7L * 24 * 60 * 60 * 1000

private const val DISK_CACHE_DIR = "image_cache"

/** `CacheStrategy.DEFAULT` của Coil 3 không bao giờ hết hạn disk cache (bỏ qua header) → tự tính TTL theo `responseMillis`. */
@OptIn(ExperimentalCoilApi::class)
private class ExpiringCacheStrategy(
    private val connectivityChecker: ConnectivityChecker,
) : CacheStrategy {

    override suspend fun read(
        cacheResponse: NetworkResponse,
        networkRequest: NetworkRequest,
        options: Options,
    ): CacheStrategy.ReadResult {
        val receivedAt = cacheResponse.responseMillis
        val age = System.currentTimeMillis() - receivedAt
        // receivedAt <= 0: entry không có mốc thời gian tin được → coi như hết hạn.
        // age < 0: đồng hồ máy bị chỉnh lùi → coi như còn hạn, đừng bắt tải lại cả cache.
        val isExpired = receivedAt <= 0L || age >= MAX_AGE_MILLIS

        // Offline thì cố ý phục vụ bản quá hạn: lỗi mạng không được xoá dữ liệu đang hiển thị.
        if (!isExpired || !connectivityChecker.isOnline()) {
            return CacheStrategy.DEFAULT.read(cacheResponse, networkRequest, options)
        }

        // Kèm header điều kiện để CDN trả 304 — Coil chỉ refresh metadata, không tải lại ảnh.
        val headers = networkRequest.headers.newBuilder()
        cacheResponse.headers[HEADER_ETAG]?.let { headers[HEADER_IF_NONE_MATCH] = it }
        cacheResponse.headers[HEADER_LAST_MODIFIED]?.let { headers[HEADER_IF_MODIFIED_SINCE] = it }
        return CacheStrategy.ReadResult(networkRequest.copy(headers = headers.build()))
    }

    override suspend fun write(
        cacheResponse: NetworkResponse?,
        networkRequest: NetworkRequest,
        networkResponse: NetworkResponse,
        options: Options,
    ): CacheStrategy.WriteResult {
        return CacheStrategy.DEFAULT.write(cacheResponse, networkRequest, networkResponse, options)
    }

    private companion object {
        const val HEADER_ETAG = "ETag"
        const val HEADER_LAST_MODIFIED = "Last-Modified"
        const val HEADER_IF_NONE_MATCH = "If-None-Match"
        const val HEADER_IF_MODIFIED_SINCE = "If-Modified-Since"
    }
}
