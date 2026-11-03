package com.base.app.core.media

import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.cache.Cache
import androidx.media3.datasource.cache.CacheEvictor
import androidx.media3.datasource.cache.CacheSpan

/**
 * Sổ dùng gần đây theo **video** (cache key), không theo span như `LeastRecentlyUsedCacheEvictor`:
 * bỏ nguyên video cũ nhất để không còn video cụt nửa trên đĩa. Thuần Kotlin để test.
 */
class RecentKeys(private val maxKeys: Int, private val maxBytes: Long) {
    /** Thứ tự truy cập: đầu = cũ nhất. */
    private val bytesByKey = LinkedHashMap<String, Long>(16, 0.75f, true)
    private var totalBytes = 0L

    fun onAdded(key: String, bytes: Long) {
        bytesByKey[key] = (bytesByKey[key] ?: 0) + bytes
        totalBytes += bytes
    }

    fun onRemoved(key: String, bytes: Long) {
        val left = (bytesByKey[key] ?: return) - bytes
        totalBytes -= bytes
        if (left <= 0) bytesByKey.remove(key) else bytesByKey[key] = left
    }

    fun touch(key: String) {
        bytesByKey[key]
    }

    /** Video cần bỏ (cũ nhất trước) để còn chỗ cho [incomingBytes]; [protected] là video đang ghi. */
    fun keysToEvict(incomingBytes: Long, protected: String): List<String> {
        var keys = bytesByKey.size
        var bytes = totalBytes + incomingBytes
        val evict = mutableListOf<String>()
        for ((key, size) in bytesByKey) {
            if (keys <= maxKeys && bytes <= maxBytes) break
            if (key == protected) continue
            evict += key
            keys--
            bytes -= size
        }
        return evict
    }
}

/**
 * Cache đĩa của player giữ tối đa [maxVideos] video gần nhất và [maxBytes]. Cache theo phiên: thư
 * mục bị xoá mỗi lần process khởi động (xem `mediaModule`).
 */
@OptIn(UnstableApi::class)
class RecentVideosEvictor(maxVideos: Int, maxBytes: Long) : CacheEvictor {
    private val keys = RecentKeys(maxVideos, maxBytes)

    override fun requiresCacheSpanTouches() = true

    override fun onCacheInitialized() = Unit

    override fun onStartFile(cache: Cache, key: String, position: Long, length: Long) {
        keys.touch(key)
        val incoming = if (length == C.LENGTH_UNSET.toLong()) 0 else length
        keys.keysToEvict(incoming, protected = key).forEach(cache::removeResource)
    }

    override fun onSpanAdded(cache: Cache, span: CacheSpan) {
        keys.onAdded(span.key, span.length)
        keys.keysToEvict(0, protected = span.key).forEach(cache::removeResource)
    }

    override fun onSpanRemoved(cache: Cache, span: CacheSpan) = keys.onRemoved(span.key, span.length)

    override fun onSpanTouched(cache: Cache, oldSpan: CacheSpan, newSpan: CacheSpan) {
        keys.touch(newSpan.key)
    }
}
