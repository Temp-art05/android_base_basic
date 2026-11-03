package com.base.app.core.ui.foundation.format

import org.junit.Assert.assertEquals
import org.junit.Test

class BidiIsolateTest {

    @Test
    fun `content text is wrapped in first strong isolate marks`() {
        assertEquals("⁨فانتازيا⁩", "فانتازيا".bidiIsolated())
    }
}
