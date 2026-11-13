package dev.pulse.client.push

/**
 * Represents a parsed Firebase Cloud Messaging (FCM) push notification payload.
 */
data class PulseFcmPayload(
    val messageId: String,
    val conversationId: String,
    val senderId: String,
    val body: String,
    val sequence: Long,
    val callRoomId: String? = null
)

/**
 * Utility parser for inbound FCM remote data messages.
 */
object PulseFcmHandler {

    /**
     * Parses the raw data map received in FirebaseMessagingService into a typed PulseFcmPayload.
     * Returns null if required Pulse fields are absent.
     */
    fun parseData(data: Map<String, String>): PulseFcmPayload? {
        val msgId = data["msgId"] ?: return null
        val convId = data["convId"] ?: return null
        val senderId = data["senderId"] ?: return null
        val body = data["body"] ?: return null
        val seq = data["convSeq"]?.toLongOrNull() ?: 0L
        val callRoomId = data["callRoomId"]

        return PulseFcmPayload(
            messageId = msgId,
            conversationId = convId,
            senderId = senderId,
            body = body,
            sequence = seq,
            callRoomId = callRoomId
        )
    }

    /**
     * Verifies whether the incoming push message corresponds to a VoIP/WebRTC call request.
     */
    fun isIncomingCall(payload: PulseFcmPayload): Boolean {
        return !payload.callRoomId.isNullOrBlank()
    }
}

