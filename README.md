# Pulse Client SDK for Android & Kotlin (`pulse-sdk-android`)

Official native Android & Kotlin SDK for the **Pulse Distributed Messaging Platform**.

Features binary WebSocket communication (`pulse.protocol.v1.msgpack`), monotonic gapless synchronization (`convSeq`), and reactive StateFlow integration.

## 📦 Installation (Gradle)

```kotlin
// In build.gradle.kts (Module: app)
dependencies {
    implementation("dev.pulse:pulse-sdk-android:1.0.0")
}
```

## 🚀 Quick Start

### 1. Initialize PulseClient

```kotlin
import dev.pulse.client.PulseClient
import dev.pulse.client.PulseConfig

val config = PulseConfig(
    gatewayUrl = "wss://gateway.pulse.dev/ws",
    token = "USER_JWT_TOKEN",
    deviceId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
)

val client = PulseClient(config)
client.connect()
```

### 2. Observe Messages in Jetpack Compose

```kotlin
@Composable
fun ChatScreen(client: PulseClient, convId: String) {
    val messagesMap by client.messages.collectAsState()
    val messages = messagesMap[convId] ?: emptyList()

    LazyColumn {
        items(messages) { message ->
            Text(text = message.body)
        }
    }
}
```

