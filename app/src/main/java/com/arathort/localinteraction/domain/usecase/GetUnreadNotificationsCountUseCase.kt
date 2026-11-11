package com.arathort.localinteraction.domain.usecase

import com.arathort.localinteraction.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetUnreadNotificationsCountUseCase @Inject constructor(
    private val notificationRepository: NotificationRepository
) {
    operator fun invoke(): Flow<Int> = notificationRepository.unreadCount

    suspend fun refresh(): Result<Int> = notificationRepository.refreshUnreadCount()
}
