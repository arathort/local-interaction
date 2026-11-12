package com.arathort.localinteraction.presentation.notifications

import com.arathort.localinteraction.domain.model.AppNotification
import com.arathort.localinteraction.domain.model.User
import com.arathort.localinteraction.domain.model.UserRole

enum class NotificationsFilter {
    ALL,
    UNREAD_ONLY
}

data class NotificationsState(
    val currentUser: User = User(id = "user-guest", role = UserRole.GUEST),
    val filter: NotificationsFilter = NotificationsFilter.ALL,
    val notifications: List<AppNotification> = emptyList(),
    val unreadCount: Int = 0,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isMarkingAllAsRead: Boolean = false,
    val errorMessage: String? = null
)
