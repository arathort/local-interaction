package com.arathort.localinteraction.domain.usecase

import com.arathort.localinteraction.domain.model.NotificationFeed
import com.arathort.localinteraction.domain.repository.NotificationRepository
import javax.inject.Inject

class GetNotificationsUseCase @Inject constructor(
    private val notificationRepository: NotificationRepository
) {
    suspend operator fun invoke(
        unreadOnly: Boolean = false,
        skip: Int = 0,
        limit: Int = 50
    ): Result<NotificationFeed> =
        notificationRepository.getNotifications(
            unreadOnly = unreadOnly,
            skip = skip,
            limit = limit
        )
}
