package com.arathort.localinteraction.domain.usecase

import com.arathort.localinteraction.domain.repository.NotificationRepository
import javax.inject.Inject

class MarkAllNotificationsAsReadUseCase @Inject constructor(
    private val notificationRepository: NotificationRepository
) {
    suspend operator fun invoke(): Result<Unit> =
        notificationRepository.markAllNotificationsAsRead()
}
