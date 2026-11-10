package com.arathort.localinteraction.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DirectMessageCreateDto(
    @SerialName("recipient_id") val recipientId: String,
    @SerialName("event_id") val eventId: String? = null,
    @SerialName("text") val text: String
)

@Serializable
data class DirectMessageResponseDto(
    @SerialName("id") val id: String,
    @SerialName("sender_id") val senderId: String,
    @SerialName("sender_name") val senderName: String,
    @SerialName("sender_avatar_url") val senderAvatarUrl: String? = null,
    @SerialName("recipient_id") val recipientId: String,
    @SerialName("recipient_name") val recipientName: String,
    @SerialName("recipient_avatar_url") val recipientAvatarUrl: String? = null,
    @SerialName("event_id") val eventId: String? = null,
    @SerialName("text") val text: String,
    @SerialName("is_read") val isRead: Boolean = false,
    @SerialName("created_at") val createdAt: String
)

@Serializable
data class ConversationSummaryResponseDto(
    @SerialName("interlocutor_id") val interlocutorId: String,
    @SerialName("interlocutor_name") val interlocutorName: String,
    @SerialName("interlocutor_role") val interlocutorRole: String,
    @SerialName("interlocutor_avatar_url") val interlocutorAvatarUrl: String? = null,
    @SerialName("last_message_text") val lastMessageText: String,
    @SerialName("last_message_at") val lastMessageAt: String,
    @SerialName("unread_count") val unreadCount: Int = 0
)
