package dev.pulse.client

import dev.pulse.client.engine.SyncEngine
import dev.pulse.client.models.*
import dev.pulse.client.transport.TransportListener
import dev.pulse.client.transport.WebSocketTransport
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject
import java.util.UUID

/**
 * Main Android client facade for the Pulse messaging platform.
 */
class PulseClient(val config: PulseConfig) : TransportListener {

    private val syncEngine = SyncEngine()
    private val transport = WebSocketTransport(config, this)

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _conversations = MutableStateFlow<List<PulseConversation>>(emptyList())
    val conversations: StateFlow<List<PulseConversation>> = _conversations.asStateFlow()

    private val _messages = MutableStateFlow<Map<String, List<PulseMessage>>>(emptyMap())
    val messages: StateFlow<Map<String, List<PulseMessage>>> = _messages.asStateFlow()

    fun connect() {
        transport.connect()
    }

    fun disconnect() {
        transport.disconnect()
        _isConnected.value = false
    }

    fun sendMessage(convId: String, body: String) {
        val clientMsgId = UUID.randomUUID().toString()
        val pending = PulseMessage(
            id = clientMsgId,
            convId = convId,
            convSeq = 0L,
            senderId = "me",
            body = body,
            status = DeliveryStatus.PENDING
        )

        // Optimistic UI update
        val current = _messages.value.toMutableMap()
        val list = (current[convId] ?: emptyList()).toMutableList()
        list.add(pending)
        current[convId] = list
        _messages.value = current

        val payload = JSONObject().apply {
            put("clientMsgId", clientMsgId)
            put("convId", convId)
            put("body", body)
        }.toString().toByteArray(Charsets.UTF_8)

        transport.sendFrame(FrameOpcode.SEND, payload)
    }

    override fun onConnected() {
        _isConnected.value = true

        val auth = JSONObject().apply {
            put("token", config.token)
            put("platform", config.platform)
            put("deviceId", config.deviceId)
        }.toString().toByteArray(Charsets.UTF_8)

        transport.sendFrame(FrameOpcode.AUTH_REQ, auth)
    }

    override fun onDisconnected(throwable: Throwable?) {
        _isConnected.value = false
    }

    override fun onFrameReceived(opcode: FrameOpcode, payload: ByteArray) {
        when (opcode) {
            FrameOpcode.AUTH_ACK -> {}
            FrameOpcode.PING -> transport.sendFrame(FrameOpcode.PONG, ByteArray(0))
            FrameOpcode.MSG_DELIVER -> handleInboundMessage(payload)
            else -> {}
        }
    }

    private fun handleInboundMessage(payload: ByteArray) {
        try {
            val json = JSONObject(String(payload, Charsets.UTF_8))
            val msg = PulseMessage(
                id = json.getString("msgId"),
                convId = json.getString("convId"),
                convSeq = json.getLong("convSeq"),
                senderId = json.getString("senderId"),
                body = json.getString("body"),
                status = DeliveryStatus.SENT
            )

            val result = syncEngine.processIncoming(msg)
            if (result.shouldDeliver) {
                val current = _messages.value.toMutableMap()
                val list = (current[msg.convId] ?: emptyList()).toMutableList()
                list.add(msg)
                current[msg.convId] = list
                _messages.value = current
            }
        } catch (_: Exception) {}
    }
}

