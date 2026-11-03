package com.base.app.core.ads

import java.util.concurrent.atomic.AtomicBoolean
import timber.log.Timber

internal const val ADS_TAG = "Ads"

/** Chạy lời gọi lib; `false` khi lib ném — caller coi như không có ad, app đi tiếp. */
internal inline fun safely(what: String, block: () -> Unit): Boolean = try {
    block()
    true
} catch (e: Exception) {
    Timber.tag(ADS_TAG).w(e, "Ads %s failed", what)
    false
}

/** Callback chỉ chạy lần đầu — lib có thể gọi lại nhiều lần (đóng + lỗi show…). */
internal fun once(block: () -> Unit): () -> Unit {
    val called = AtomicBoolean(false)
    return { if (called.compareAndSet(false, true)) block() }
}

internal fun <T> onceWith(block: (T) -> Unit): (T) -> Unit {
    val called = AtomicBoolean(false)
    return { value -> if (called.compareAndSet(false, true)) block(value) }
}
