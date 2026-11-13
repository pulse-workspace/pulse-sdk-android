plugins {
    kotlin("jvm") version "1.9.23"
}

group = "dev.pulse"
version = "1.0.0"

repositories {
    mavenCentral()
    google()
}

dependencies {
    // Coroutines for asynchronous streaming
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")

    // OkHttp for binary WebSocket communication
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // JSON serialization
    implementation("org.json:json:20240303")

    // Testing
    testImplementation(kotlin("test"))
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.0")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}

