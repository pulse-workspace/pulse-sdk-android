package dev.pulse.client.transport

import dev.pulse.client.PulseConfig
import dev.pulse.client.models.FrameOpcode
import okhttp3.*
import okio.ByteString
import okio.ByteString.Companion.toByteString
import java.nio.ByteBuffer
import java.nio.ByteOrder

interface TransportListener {
    fun onConnected()
    fun onDisconnected(throwable: Throwable?)
    fun onFrameReceived(opcode: FrameOpcode, payload: ByteArray)
}

/**
 * Binary WebSocket transport for Android using OkHttp.
 */
class WebSocketTransport(
    private val config: PulseConfig,
    private val listener: TransportListener
) : WebSocketListener() {

    private val client = OkHttpClient.Builder().build()
    private var webSocket: WebSocket? = null
    private var isConnected = false

    fun connect() {
        val request = Request.Builder()
            .url(config.gatewayUrl)
            .header("Sec-WebSocket-Protocol", "pulse.protocol.v1.msgpack")
            .build()

        webSocket = client.newWebSocket(request, this)
    }

    fun disconnect() {
        webSocket?.close(1000, "Normal closure")
        isConnected = false
    }

    fun sendFrame(opcode: FrameOpcode, payload: ByteArray) {
        val buffer = ByteBuffer.allocate(7 + payload.size).order(ByteOrder.BIG_ENDIAN)
        buffer.put(0x50.toByte()) // Magic 'P'
        buffer.putShort(opcode.code)
        buffer.putInt(payload.size)
        buffer.put(payload)

        val byteString = buffer.array().toByteString()
        webSocket?.send(byteString)
    }

    override fun onOpen(webSocket: WebSocket, response: Response) {
        isConnected = true
        listener.onConnected()
    }

    override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
        val array = bytes.toByteArray()
        if (array.size < 7) return

        val buffer = ByteBuffer.wrap(array).order(ByteOrder.BIG_ENDIAN)
        val magic = buffer.get()
        if (magic != 0x50.toByte()) return

        val opCode = buffer.short
        val length = buffer.int

        if (array.size < 7 + length) return
        val payload = ByteArray(length)
        buffer.get(payload)

        val opcode = FrameOpcode.fromCode(opCode) ?: return
        listener.onFrameReceived(opcode, payload)
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        isConnected = false
        listener.onDisconnected(t)
    }

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        isConnected = false
        listener.onDisconnected(null)
    }
}

