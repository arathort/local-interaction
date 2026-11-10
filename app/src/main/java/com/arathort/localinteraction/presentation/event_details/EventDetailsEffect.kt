package com.arathort.localinteraction.presentation.event_details

import androidx.annotation.StringRes

sealed interface EventDetailsEffect {
    data object NavigateBack : EventDetailsEffect
    data object NavigateToAuth : EventDetailsEffect
    data class NavigateToManageApplications(val eventId: String) : EventDetailsEffect
    data class NavigateToEditEvent(val eventId: String) : EventDetailsEffect
    data class NavigateToProfile(val userId: String) : EventDetailsEffect
    data class ShowSnackbar(@param:StringRes val messageResId: Int) : EventDetailsEffect
    data class ShowSnackbarText(val message: String) : EventDetailsEffect
}
