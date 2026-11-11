package com.arathort.localinteraction.domain.repository

import com.arathort.localinteraction.domain.model.AppNotification
import com.arathort.localinteraction.domain.model.NotificationFeed
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    val unreadCount: Flow<Int>

    suspend fun getNotifications(
        unreadOnly: Boolean = false,
        skip: Int = 0,
        limit: Int = 50
    ): Result<NotificationFeed>

    suspend fun markNotificationAsRead(notificationId: String): Result<AppNotification>

    suspend fun markAllNotificationsAsRead(): Result<Unit>

    suspend fun refreshUnreadCount(): Result<Int>
}
