package dev.pulse.client

import dev.pulse.client.engine.SyncEngine
import dev.pulse.client.models.PulseMessage
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SyncEngineTest {

    @Test
    fun testInOrderSequencing() {
        val engine = SyncEngine()
        val convId = "conv_android"

        val msg1 = PulseMessage(id = "1", convId = convId, convSeq = 1L, senderId = "usr_1", body = "Hello")
        val res1 = engine.processIncoming(msg1)
        assertFalse(res1.hasGap)
        assertTrue(res1.shouldDeliver)
        assertEquals(1L, engine.getLastKnownSequence(convId))

        val msg2 = PulseMessage(id = "2", convId = convId, convSeq = 2L, senderId = "usr_1", body = "World")
        val res2 = engine.processIncoming(msg2)
        assertFalse(res2.hasGap)
        assertTrue(res2.shouldDeliver)
        assertEquals(2L, engine.getLastKnownSequence(convId))
    }

    @Test
    fun testGapDetectionAndResyncDrain() {
        val engine = SyncEngine()
        val convId = "conv_android"
        engine.setLastKnownSequence(convId, 1L)

        // Incoming seq 3 while last known is 1 -> gap
        val msg3 = PulseMessage(id = "3", convId = convId, convSeq = 3L, senderId = "usr_1", body = "Future")
        val res3 = engine.processIncoming(msg3)
        assertTrue(res3.hasGap)
        assertFalse(res3.shouldDeliver)

        // Catch-up slice arrives: msg 2
        val msg2 = PulseMessage(id = "2", convId = convId, convSeq = 2L, senderId = "usr_1", body = "Missing")
        val delivered = engine.applyResyncBatch(convId, listOf(msg2))

        assertEquals(2, delivered.size)
        assertEquals(2L, delivered[0].convSeq)
        assertEquals(3L, delivered[1].convSeq)
        assertEquals(3L, engine.getLastKnownSequence(convId))
    }
}

