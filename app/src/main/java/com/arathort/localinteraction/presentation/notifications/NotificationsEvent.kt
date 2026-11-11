package com.arathort.localinteraction.presentation.notifications

import com.arathort.localinteraction.domain.model.AppNotification

sealed interface NotificationsEvent {
    data class FilterChanged(val filter: NotificationsFilter) : NotificationsEvent
    data object RefreshRequested : NotificationsEvent
    data class NotificationClicked(val notification: AppNotification) : NotificationsEvent
    data class MarkAsReadClicked(val notificationId: String) : NotificationsEvent
    data object MarkAllAsReadClicked : NotificationsEvent
    data object LoginClicked : NotificationsEvent
}
