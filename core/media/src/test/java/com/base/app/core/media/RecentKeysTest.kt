package com.base.app.core.media

import org.junit.Assert.assertEquals
import org.junit.Test

class RecentKeysTest {

    @Test
    fun `nothing to evict while under both limits`() {
        val keys = RecentKeys(maxKeys = 3, maxBytes = 1_000)
        keys.onAdded("a", 100)
        keys.onAdded("b", 100)
        assertEquals(emptyList<String>(), keys.keysToEvict(incomingBytes = 0, protected = "b"))
    }

    @Test
    fun `more videos than allowed - the least recently used one goes`() {
        val keys = RecentKeys(maxKeys = 2, maxBytes = 10_000)
        keys.onAdded("a", 100)
        keys.onAdded("b", 100)
        keys.onAdded("c", 100)
        assertEquals(listOf("a"), keys.keysToEvict(incomingBytes = 0, protected = "c"))
    }

    @Test
    fun `touching a video makes it recent again`() {
        val keys = RecentKeys(maxKeys = 2, maxBytes = 10_000)
        keys.onAdded("a", 100)
        keys.onAdded("b", 100)
        keys.touch("a")
        keys.onAdded("c", 100)
        assertEquals(listOf("b"), keys.keysToEvict(incomingBytes = 0, protected = "c"))
    }

    @Test
    fun `over the byte budget - drop oldest videos until the incoming bytes fit`() {
        val keys = RecentKeys(maxKeys = 10, maxBytes = 1_000)
        keys.onAdded("a", 400)
        keys.onAdded("b", 400)
        keys.onAdded("c", 100)
        assertEquals(listOf("a", "b"), keys.keysToEvict(incomingBytes = 600, protected = "c"))
    }

    @Test
    fun `the video being written is never evicted even if it is the oldest`() {
        val keys = RecentKeys(maxKeys = 1, maxBytes = 10_000)
        keys.onAdded("a", 100)
        keys.onAdded("b", 100)
        keys.touch("a")
        assertEquals(listOf("b"), keys.keysToEvict(incomingBytes = 0, protected = "a"))
    }

    @Test
    fun `removed spans give their bytes back and an empty video is forgotten`() {
        val keys = RecentKeys(maxKeys = 1, maxBytes = 10_000)
        keys.onAdded("a", 100)
        keys.onAdded("a", 50)
        keys.onRemoved("a", 150)
        keys.onAdded("b", 100)
        assertEquals(emptyList<String>(), keys.keysToEvict(incomingBytes = 0, protected = "b"))
    }
}
