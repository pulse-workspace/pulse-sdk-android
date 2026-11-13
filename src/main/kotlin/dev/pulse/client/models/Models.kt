package dev.pulse.client.models

/**
 * Message delivery state tracking optimistic outbox lifecycle.
 */
enum class DeliveryStatus {
    PENDING,
    SENT,
    FAILED
}

/**
 * Chat message entity model.
 */
data class PulseMessage(
    val id: String,
    val convId: String,
    val convSeq: Long,
    val senderId: String,
    val body: String,
    val timestamp: Long = System.currentTimeMillis(),
    val status: DeliveryStatus = DeliveryStatus.SENT
)

/**
 * Conversation entity model representing direct and group channels.
 */
data class PulseConversation(
    val id: String,
    val type: String,
    val name: String?,
    val lastConvSeq: Long = 0L,
    val lastMessageText: String? = null,
    val unreadCount: Int = 0
)

/**
 * 16-bit binary WebSocket opcodes.
 */
enum class FrameOpcode(val code: Short) {
    AUTH_REQ(0x0001),
    AUTH_ACK(0x0002),
    PING(0x0003),
    PONG(0x0004),
    SEND(0x0010),
    ACK(0x0011),
    MSG_DELIVER(0x0012),
    RESYNC(0x0020),
    RESYNC_BATCH(0x0021),
    HANDOFF_REQ(0x0030),
    HANDOFF_DONE(0x0032);

    companion object {
        fun fromCode(code: Short): FrameOpcode? = entries.find { it.code == code }
    }
}

