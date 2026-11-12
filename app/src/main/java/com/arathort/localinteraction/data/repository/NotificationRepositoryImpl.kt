package com.arathort.localinteraction.data.repository

import com.arathort.localinteraction.data.datasource.remote.LocalInteractionApiService
import com.arathort.localinteraction.data.mapper.toDomain
import com.arathort.localinteraction.domain.model.AppNotification
import com.arathort.localinteraction.domain.model.NotificationFeed
import com.arathort.localinteraction.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepositoryImpl @Inject constructor(
    private val apiService: LocalInteractionApiService
) : NotificationRepository {

    private val _unreadCount = MutableStateFlow(0)
    override val unreadCount: Flow<Int> = _unreadCount.asStateFlow()

    override suspend fun getNotifications(
        unreadOnly: Boolean,
        skip: Int,
        limit: Int
    ): Result<NotificationFeed> = runCatching {
        val response = apiService.getMyNotifications(
            unreadOnly = if (unreadOnly) true else null,
            skip = skip,
            limit = limit
        ).toDomain()
        _unreadCount.value = response.unreadCount
        response
    }

    override suspend fun markNotificationAsRead(notificationId: String): Result<AppNotification> = runCatching {
        val updated = apiService.markNotificationAsRead(notificationId).toDomain()
        _unreadCount.value = maxOf(0, _unreadCount.value - 1)
        updated
    }

    override suspend fun markAllNotificationsAsRead(): Result<Unit> = runCatching {
        apiService.markAllNotificationsAsRead()
        _unreadCount.value = 0
    }

    override suspend fun refreshUnreadCount(): Result<Int> = runCatching {
        val response = apiService.getMyNotifications(unreadOnly = true, limit = 1).toDomain()
        _unreadCount.value = response.unreadCount
        response.unreadCount
    }
}
