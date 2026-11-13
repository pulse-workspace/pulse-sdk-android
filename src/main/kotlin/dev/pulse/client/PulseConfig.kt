package dev.pulse.client

/**
 * Configuration options required to instantiate PulseClient for Android.
 * Enforces fail-fast validation with zero silent default fallbacks.
 */
data class PulseConfig(
    val gatewayUrl: String,
    val token: String,
    val deviceId: String,
    val platform: String = "ANDROID",
    val heartbeatIntervalMs: Long = 15000L,
    val reconnectBaseDelayMs: Long = 1000L,
    val ackTimeoutMs: Long = 5000L
) {
    init {
        require(gatewayUrl.isNotBlank()) { "gatewayUrl cannot be blank" }
        require(gatewayUrl.startsWith("ws://") || gatewayUrl.startsWith("wss://")) {
            "gatewayUrl must start with ws:// or wss://"
        }
        require(token.isNotBlank()) { "token cannot be blank" }
        require(deviceId.isNotBlank()) { "deviceId cannot be blank" }
        require(heartbeatIntervalMs > 0) { "heartbeatIntervalMs must be greater than zero" }
        require(reconnectBaseDelayMs > 0) { "reconnectBaseDelayMs must be greater than zero" }
        require(ackTimeoutMs > 0) { "ackTimeoutMs must be greater than zero" }
    }
}

