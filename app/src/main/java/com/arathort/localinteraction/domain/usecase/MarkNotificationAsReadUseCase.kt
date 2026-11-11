package com.arathort.localinteraction.domain.usecase

import com.arathort.localinteraction.domain.model.AppNotification
import com.arathort.localinteraction.domain.repository.NotificationRepository
import javax.inject.Inject

class MarkNotificationAsReadUseCase @Inject constructor(
    private val notificationRepository: NotificationRepository
) {
    suspend operator fun invoke(notificationId: String): Result<AppNotification> =
        notificationRepository.markNotificationAsRead(notificationId)
}
