package com.arathort.localinteraction.data.mapper

import com.arathort.localinteraction.data.dto.NotificationListResponseDto
import com.arathort.localinteraction.data.dto.NotificationResponseDto
import com.arathort.localinteraction.domain.model.AppNotification
import com.arathort.localinteraction.domain.model.NotificationFeed
import com.arathort.localinteraction.domain.model.NotificationType

fun NotificationResponseDto.toDomain(): AppNotification =
    AppNotification(
        id = id,
        userId = userId,
        type = NotificationType.fromApiValue(type),
        title = title,
        message = message,
        relatedEntityId = relatedEntityId,
        isRead = isRead,
        createdAt = createdAt
    )

fun NotificationListResponseDto.toDomain(): NotificationFeed =
    NotificationFeed(
        unreadCount = unreadCount,
        items = items.map { it.toDomain() }
    )
