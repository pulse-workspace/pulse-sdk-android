package dev.pulse.client.ui

import dev.pulse.client.models.DeliveryStatus

/**
 * State representation for rendering conversation list items in Jetpack Compose.
 */
data class ConversationListItemUiState(
    val id: String,
    val title: String,
    val subtitle: String,
    val timestampFormatted: String,
    val unreadBadgeCount: Int,
    val isGroup: Boolean
)

/**
 * State representation for individual message bubbles in a Compose chat screen.
 */
data class MessageBubbleUiState(
    val id: String,
    val senderName: String,
    val body: String,
    val isFromMe: Boolean,
    val deliveryStatus: DeliveryStatus,
    val timeLabel: String
)

/**
 * View state for the message input composer bar.
 */
data class ChatInputUiState(
    val text: String = "",
    val isSending: Boolean = false,
    val isRecordingVoice: Boolean = false,
    val canSend: Boolean = false
) {
    /**
     * Determines whether the send action button should be enabled.
     */
    fun hasValidInput(): Boolean = text.isNotBlank() && !isSending
}

