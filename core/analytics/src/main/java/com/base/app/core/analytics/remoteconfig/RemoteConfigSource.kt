package com.base.app.core.analytics.remoteconfig

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Nguồn remote config. Là interface để base chạy được khi chưa cấu hình Firebase,
 * và để test thay bằng fake mà không cần Firebase emulator.
 */
interface RemoteConfigSource {
    val isReady: StateFlow<Boolean>

    /** Fetch từ remote rồi sync xuống local. Gọi một lần lúc app start. */
    suspend fun fetchAndSync(keys: List<RemoteConfigKey<*>> = RemoteKeys.ALL_KEYS): Boolean

    /**
     * Chờ fetch tối đa [timeoutMs] (Splash). Hết giờ → `false`, app chạy với giá trị phiên trước /
     * default; fetch vẫn chạy tiếp ở nền (single-flight, không huỷ theo người chờ).
     */
    suspend fun awaitFetch(timeoutMs: Long = DEFAULT_AWAIT_MS): Boolean =
        withTimeoutOrNull(timeoutMs) { fetchAndSync() } ?: false

    fun <T> get(key: RemoteConfigKey<T>): T
    fun <T> observe(key: RemoteConfigKey<T>): Flow<T>
    suspend fun <T> getAsync(key: RemoteConfigKey<T>): T
}

const val DEFAULT_AWAIT_MS = 3_000L

/**
 * Dùng khi chưa có google-services.json: luôn trả default đã khai báo trong key.
 * Nhờ vậy code feature không phải biết remote config có sẵn sàng hay không.
 */
object NoOpRemoteConfigSource : RemoteConfigSource {
    override val isReady: StateFlow<Boolean> =
        MutableStateFlow(true)

    override suspend fun fetchAndSync(keys: List<RemoteConfigKey<*>>): Boolean = false
    override fun <T> get(key: RemoteConfigKey<T>): T = key.default
    override fun <T> observe(key: RemoteConfigKey<T>): Flow<T> = flowOf(key.default)
    override suspend fun <T> getAsync(key: RemoteConfigKey<T>): T = key.default
}
