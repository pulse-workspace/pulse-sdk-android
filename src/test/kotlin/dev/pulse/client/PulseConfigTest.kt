package dev.pulse.client

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class PulseConfigTest {

    @Test
    fun testValidConfiguration() {
        val config = PulseConfig(
            gatewayUrl = "wss://gateway.pulse.dev/ws",
            token = "jwt_token_sample",
            deviceId = "pixel_8_pro"
        )
        assertEquals("wss://gateway.pulse.dev/ws", config.gatewayUrl)
        assertEquals("jwt_token_sample", config.token)
    }

    @Test
    fun testInvalidUrlSchemeFails() {
        assertFailsWith<IllegalArgumentException> {
            PulseConfig(
                gatewayUrl = "http://gateway.pulse.dev",
                token = "jwt_token_sample",
                deviceId = "pixel_8_pro"
            )
        }
    }

    @Test
    fun testBlankTokenFails() {
        assertFailsWith<IllegalArgumentException> {
            PulseConfig(
                gatewayUrl = "wss://gateway.pulse.dev/ws",
                token = "   ",
                deviceId = "pixel_8_pro"
            )
        }
    }
}

