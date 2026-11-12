package com.arathort.localinteraction.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NotificationResponseDto(
    @SerialName("id") val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("type") val type: String,
    @SerialName("title") val title: String,
    @SerialName("message") val message: String,
    @SerialName("related_entity_id") val relatedEntityId: String? = null,
    @SerialName("is_read") val isRead: Boolean = false,
    @SerialName("created_at") val createdAt: String
)

@Serializable
data class NotificationListResponseDto(
    @SerialName("unread_count") val unreadCount: Int = 0,
    @SerialName("items") val items: List<NotificationResponseDto> = emptyList()
)
