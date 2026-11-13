package dev.pulse.client.engine

import dev.pulse.client.models.PulseMessage

data class ProcessResult(
    val hasGap: Boolean,
    val shouldDeliver: Boolean
)

/**
 * Manages monotonic sequence tracking (`convSeq`) and catch-up ordering for Android.
 */
class SyncEngine {
    private val sequenceMap = mutableMapOf<String, Long>()
    private val pendingBuffers = mutableMapOf<String, MutableList<PulseMessage>>()
    private val lock = Any()

    fun setLastKnownSequence(convId: String, seq: Long) {
        synchronized(lock) {
            sequenceMap[convId] = seq
        }
    }

    fun getLastKnownSequence(convId: String): Long {
        synchronized(lock) {
            return sequenceMap[convId] ?: 0L
        }
    }

    fun processIncoming(message: PulseMessage): ProcessResult {
        synchronized(lock) {
            val lastSeq = sequenceMap[message.convId] ?: 0L

            if (message.convSeq <= lastSeq) {
                return ProcessResult(hasGap = false, shouldDeliver = false)
            }

            if (message.convSeq == lastSeq + 1) {
                sequenceMap[message.convId] = message.convSeq
                return ProcessResult(hasGap = false, shouldDeliver = true)
            }

            // Gap detected: sequence jumped
            val buffer = pendingBuffers.getOrPut(message.convId) { mutableListOf() }
            buffer.add(message)
            return ProcessResult(hasGap = true, shouldDeliver = false)
        }
    }

    fun applyResyncBatch(convId: String, batch: List<PulseMessage>): List<PulseMessage> {
        synchronized(lock) {
            val delivered = mutableListOf<PulseMessage>()
            val sortedBatch = batch.sortedBy { it.convSeq }

            for (msg in sortedBatch) {
                val lastSeq = sequenceMap[convId] ?: 0L
                if (msg.convSeq == lastSeq + 1) {
                    sequenceMap[convId] = msg.convSeq
                    delivered.add(msg)
                }
            }

            // Drain buffered pending messages
            val buffer = pendingBuffers[convId]
            if (buffer != null) {
                buffer.sortBy { it.convSeq }
                val remaining = mutableListOf<PulseMessage>()

                for (msg in buffer) {
                    val lastSeq = sequenceMap[convId] ?: 0L
                    if (msg.convSeq == lastSeq + 1) {
                        sequenceMap[convId] = msg.convSeq
                        delivered.add(msg)
                    } else if (msg.convSeq > lastSeq + 1) {
                        remaining.add(msg)
                    }
                }
                pendingBuffers[convId] = remaining
            }

            return delivered
        }
    }
}

