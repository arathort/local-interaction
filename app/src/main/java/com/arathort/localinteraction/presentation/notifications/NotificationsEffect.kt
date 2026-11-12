package com.arathort.localinteraction.presentation.notifications

import androidx.annotation.StringRes

sealed interface NotificationsEffect {
    data class ShowSnackbar(@param:StringRes val messageResId: Int) : NotificationsEffect
    data class ShowSnackbarText(val message: String) : NotificationsEffect
    data class NavigateToEventDetails(val eventId: String) : NotificationsEffect
    data object NavigateToAuth : NotificationsEffect
}
