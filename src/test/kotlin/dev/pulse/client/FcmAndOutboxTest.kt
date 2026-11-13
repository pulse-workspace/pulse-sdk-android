package dev.pulse.client

import dev.pulse.client.push.PulseFcmHandler
import dev.pulse.client.push.PulseFcmPayload
import dev.pulse.client.storage.PulseOutbox
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class FcmAndOutboxTest {

    @Test
    fun `test valid FCM payload parsing`() {
        val data = mapOf(
            "msgId" to "fcm-msg-001",
            "convId" to "conv-android",
            "senderId" to "user-bob",
            "body" to "Hello Kotlin!",
            "convSeq" to "105",
            "callRoomId" to "webrtc-call-12"
        )

        val payload = PulseFcmHandler.parseData(data)
        assertNotNull(payload)
        assertEquals("fcm-msg-001", payload?.messageId)
        assertEquals("conv-android", payload?.conversationId)
        assertEquals("user-bob", payload?.senderId)
        assertEquals("Hello Kotlin!", payload?.body)
        assertEquals(105L, payload?.sequence)
        assertEquals("webrtc-call-12", payload?.callRoomId)
        assertTrue(PulseFcmHandler.isIncomingCall(payload!!))
    }

    @Test
    fun `test invalid FCM payload returns null`() {
        val emptyData = mapOf("unrelated" to "data")
        assertNull(PulseFcmHandler.parseData(emptyData))
    }

    @Test
    fun `test outbox queue lifecycle and retry logic`() {
        val outbox = PulseOutbox()
        assertTrue(outbox.pendingEntries().isEmpty())

        val entry1 = outbox.enqueue("conv-1", "Msg 1")
        val entry2 = outbox.enqueue("conv-1", "Msg 2")
        assertEquals(2, outbox.pendingEntries().size)

        outbox.markRetry(entry1.id)
        val updated = outbox.pendingEntries().first { it.id == entry1.id }
        assertEquals(1, updated.retryCount)

        outbox.acknowledge(entry1.id)
        assertEquals(1, outbox.pendingEntries().size)
        assertEquals(entry2.id, outbox.pendingEntries().first().id)

        outbox.clear()
        assertTrue(outbox.pendingEntries().isEmpty())
    }
}

