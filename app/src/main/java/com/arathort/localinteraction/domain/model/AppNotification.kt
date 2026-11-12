package com.arathort.localinteraction.domain.model

data class AppNotification(
    val id: String,
    val userId: String,
    val type: NotificationType,
    val title: String,
    val message: String,
    val relatedEntityId: String? = null,
    val isRead: Boolean = false,
    val createdAt: String
)

data class NotificationFeed(
    val unreadCount: Int = 0,
    val items: List<AppNotification> = emptyList()
)
