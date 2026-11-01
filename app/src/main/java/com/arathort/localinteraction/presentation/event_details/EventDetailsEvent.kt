package com.arathort.localinteraction.presentation.event_details

import com.arathort.localinteraction.domain.model.EventStatus

sealed interface EventDetailsEvent {
    data object Refresh : EventDetailsEvent
    data object ApplyClicked : EventDetailsEvent
    data class MotivationLetterChanged(val text: String) : EventDetailsEvent
    data class AgreedToShareContactsChanged(val agreed: Boolean) : EventDetailsEvent
    data object SubmitApplication : EventDetailsEvent
    data object DismissApplyDialog : EventDetailsEvent
    data object WithdrawApplicationClicked : EventDetailsEvent
    data object ManageApplicationsClicked : EventDetailsEvent
    data object ChangeStatusClicked : EventDetailsEvent
    data class NewStatusSelected(val status: EventStatus) : EventDetailsEvent
    data object SubmitStatusChange : EventDetailsEvent
    data object DismissStatusDialog : EventDetailsEvent
    data object BackClicked : EventDetailsEvent
}
