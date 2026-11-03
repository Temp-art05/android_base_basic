package com.base.app.core.media

import org.junit.Assert.assertEquals
import org.junit.Test

class PlayerPoolTest {

    private val normal = MediaProfile.forTier(DeviceTier.NORMAL)
    private val low = MediaProfile.forTier(DeviceTier.LOW)
    private val allFree: (Int) -> Boolean = { true }

    @Test
    fun `normal device keeps the current, next and previous episode ready - current first`() {
        assertEquals(listOf(5, 6, 4), poolPositions(current = 5, size = 10, profile = normal, isPlayable = allFree))
        assertEquals(3, normal.poolSize)
    }

    @Test
    fun `low device also keeps the previous one - swiping back must be instant everywhere`() {
        assertEquals(listOf(5, 6, 4), poolPositions(current = 5, size = 10, profile = low, isPlayable = allFree))
        assertEquals(3, low.poolSize)
    }

    @Test
    fun `positions are clipped at both ends and locked episodes get no player`() {
        assertEquals(listOf(0, 1), poolPositions(current = 0, size = 10, profile = normal, isPlayable = allFree))
        assertEquals(listOf(9, 8), poolPositions(current = 9, size = 10, profile = normal, isPlayable = allFree))
        assertEquals(listOf(4), poolPositions(current = 5, size = 10, profile = normal, isPlayable = { it < 5 || it == 4 }))
    }

    @Test
    fun `neighbour buffer caps - next 5s and previous 2s, low device 2_5s and 1s`() {
        assertEquals(5_000L, normal.neighborBufferMs[1])
        assertEquals(2_000L, normal.neighborBufferMs[-1])
        assertEquals(2_500L, low.neighborBufferMs[1])
        assertEquals(1_000L, low.neighborBufferMs[-1])
    }

    @Test
    fun `first assignment fills free slots in priority order`() {
        assertEquals(listOf(5, 6, 4), assignSlots(previous = listOf(null, null, null), wanted = listOf(5, 6, 4)))
    }

    @Test
    fun `swiping forward keeps the players that already hold a wanted episode`() {
        // Slot 0 phát 5, slot 1 giữ 6, slot 2 giữ 4. Vuốt tới 6: cần 6, 7, 5.
        val after = assignSlots(previous = listOf(5, 6, 4), wanted = listOf(6, 7, 5))
        assertEquals(5, after[0])
        assertEquals(6, after[1])
        // Player của tập 4 (ra khỏi cửa sổ) được tái dùng cho tập 7.
        assertEquals(7, after[2])
    }

    @Test
    fun `slots no longer wanted are freed and no position is held twice`() {
        val after = assignSlots(previous = listOf(5, 6, 4), wanted = listOf(9))
        assertEquals(1, after.count { it == 9 })
        assertEquals(2, after.count { it == null })
    }

    @Test
    fun `wanted positions beyond the pool size are dropped from the end`() {
        assertEquals(listOf(1, 2), assignSlots(previous = listOf(null, null), wanted = listOf(1, 2, 0)))
    }
}
