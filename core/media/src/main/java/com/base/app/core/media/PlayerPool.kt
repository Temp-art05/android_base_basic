package com.base.app.core.media

import kotlin.math.abs

/**
 * Vị trí cần giữ player sẵn khi đang ở [current], theo thứ tự ưu tiên: tập hiện tại, rồi tập kế
 * (người xem vuốt tới nhiều hơn vuốt lui), rồi tập trước. Tập khoá không có player (không phát được).
 */
fun poolPositions(current: Int, size: Int, profile: MediaProfile, isPlayable: (Int) -> Boolean): List<Int> =
    (listOf(0) + profile.neighborBufferMs.keys.sortedWith(compareBy({ abs(it) }, { -it })))
        .map { current + it }
        .filter { it in 0 until size && isPlayable(it) }

/**
 * Gán vị trí cho từng player của pool. [previous] = vị trí mỗi slot đang giữ (null = rảnh). Player
 * đang giữ một vị trí vẫn cần thì giữ nguyên (không prepare lại, không mất khung hình); vị trí mới lấy
 * slot rảnh theo thứ tự ưu tiên của [wanted]. Thừa vị trí so với số slot thì bỏ phần cuối.
 */
fun assignSlots(previous: List<Int?>, wanted: List<Int>): List<Int?> {
    val slots = previous.map { position -> position?.takeIf { it in wanted } }.toMutableList()
    wanted.filter { it !in slots }.forEach { position ->
        val free = slots.indexOfFirst { it == null }
        if (free >= 0) slots[free] = position
    }
    return slots
}
