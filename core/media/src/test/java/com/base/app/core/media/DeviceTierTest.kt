package com.base.app.core.media

import org.junit.Assert.assertEquals
import org.junit.Test

class DeviceTierTest {

    private val gb = 1024L * 1024 * 1024

    @Test
    fun `system low-ram flag wins`() {
        assertEquals(DeviceTier.LOW, classifyDeviceTier(isLowRam = true, memoryClassMb = 512, totalMemBytes = 8 * gb))
    }

    @Test
    fun `small heap is low`() {
        assertEquals(DeviceTier.LOW, classifyDeviceTier(isLowRam = false, memoryClassMb = 128, totalMemBytes = 8 * gb))
        assertEquals(DeviceTier.NORMAL, classifyDeviceTier(isLowRam = false, memoryClassMb = 192, totalMemBytes = 8 * gb))
        assertEquals(DeviceTier.NORMAL, classifyDeviceTier(isLowRam = false, memoryClassMb = 256, totalMemBytes = 8 * gb))
    }

    @Test
    fun `small total RAM is low`() {
        assertEquals(DeviceTier.LOW, classifyDeviceTier(isLowRam = false, memoryClassMb = 256, totalMemBytes = 2 * gb))
        assertEquals(DeviceTier.NORMAL, classifyDeviceTier(isLowRam = false, memoryClassMb = 256, totalMemBytes = 6 * gb))
    }
}
