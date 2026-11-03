package com.base.app.core.media

import android.app.ActivityManager
import android.content.Context

/** Mức máy quyết định preload bao nhiêu, buffer bao lớn (xem [MediaProfile]). */
enum class DeviceTier { NORMAL, LOW }

/**
 * Máy yếu khi hệ thống tự báo low-RAM, heap app < 192 MB, hoặc tổng RAM < 3 GB — ngưỡng cố định để
 * hành vi preload đoán trước được, không phụ thuộc lúc chạy. Heap 192 MB là mức của máy 4 GB tầm trung → máy thường.
 */
fun classifyDeviceTier(isLowRam: Boolean, memoryClassMb: Int, totalMemBytes: Long): DeviceTier = when {
    isLowRam -> DeviceTier.LOW
    memoryClassMb < NORMAL_MEMORY_CLASS_MB -> DeviceTier.LOW
    totalMemBytes < LOW_TOTAL_MEM_BYTES -> DeviceTier.LOW
    else -> DeviceTier.NORMAL
}

/** [override] chỉ để kiểm/chụp ở flavor dev (ép một mức máy bất kể phần cứng); null = tự đo. */
class DeviceTierDetector(private val context: Context, private val override: () -> DeviceTier? = { null }) {

    fun detect(): DeviceTier {
        override()?.let { return it }
        val activityManager = context.getSystemService(ActivityManager::class.java)
        val memoryInfo = ActivityManager.MemoryInfo().also(activityManager::getMemoryInfo)
        return classifyDeviceTier(
            isLowRam = activityManager.isLowRamDevice,
            memoryClassMb = activityManager.memoryClass,
            totalMemBytes = memoryInfo.totalMem,
        )
    }
}

private const val NORMAL_MEMORY_CLASS_MB = 192
private const val LOW_TOTAL_MEM_BYTES = 3L * 1024 * 1024 * 1024
