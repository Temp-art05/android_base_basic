package com.base.app.core.utils.extension

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import timber.log.Timber

/*
 * Chỉ giữ những extension KHÔNG có sẵn trong androidx/kotlinx — trùng tên API chính thống thì
 * IDE auto-import rất dễ chọn nhầm bản tự viết.
 */

/** Log lỗi của flow rồi cho phép xử lý tiếp, không nuốt exception im lặng. */
fun <T> Flow<T>.catchAndLog(
    tag: String = "Flow",
    onError: ((Throwable) -> Unit)? = null,
): Flow<T> = catch { e ->
    Timber.e(e, "$tag: Error occurred")
    onError?.invoke(e)
}

/** Lấy một phần của state và chỉ emit khi phần đó thực sự đổi. */
fun <T, R> StateFlow<T>.select(selector: (T) -> R): Flow<R> =
    map(selector).distinctUntilChanged()

/** Chỉ cho qua emission đầu tiên trong mỗi cửa sổ thời gian — chống double-click. */
fun <T> Flow<T>.throttleFirst(windowDuration: Long): Flow<T> = flow {
    var lastEmissionTime = 0L
    collect { value ->
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastEmissionTime >= windowDuration) {
            lastEmissionTime = currentTime
            emit(value)
        }
    }
}
