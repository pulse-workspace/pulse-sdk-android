package dev.pulse.client.storage

import java.util.UUID

/**
 * Encapsulates an outbound message pending server delivery confirmation.
 */
data class OutboxEntry(
    val id: String = UUID.randomUUID().toString(),
    val convId: String,
    val body: String,
    val createdAt: Long = System.currentTimeMillis(),
    var retryCount: Int = 0
)

/**
 * Thread-safe message queue managing pending outbound frames and exponential retries.
 */
class PulseOutbox {
    private val queue = mutableListOf<OutboxEntry>()
    private val lock = Any()

    /**
     * Enqueues a newly generated outgoing message.
     */
    fun enqueue(convId: String, body: String): OutboxEntry {
        synchronized(lock) {
            val entry = OutboxEntry(convId = convId, body = body)
            queue.add(entry)
            return entry
        }
    }

    /**
     * Acknowledges receipt of a message and removes it from the pending outbox queue.
     */
    fun acknowledge(id: String) {
        synchronized(lock) {
            queue.removeAll { it.id == id }
        }
    }

    /**
     * Increments the delivery attempt counter for a failed transmission.
     */
    fun markRetry(id: String) {
        synchronized(lock) {
            val index = queue.indexOfFirst { it.id == id }
            if (index != -1) {
                queue[index].retryCount += 1
            }
        }
    }

    /**
     * Returns an immutable snapshot list of all unacknowledged outbox items.
     */
    fun pendingEntries(): List<OutboxEntry> {
        synchronized(lock) {
            return queue.toList()
        }
    }

    /**
     * Empties the pending outbox queue.
     */
    fun clear() {
        synchronized(lock) {
            queue.clear()
        }
    }
}

