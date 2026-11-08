package com.arathort.localinteraction.presentation.manage_applications

sealed interface ManageApplicationsEvent {
    data object Refresh : ManageApplicationsEvent
    data class FilterSelected(val filter: ApplicationStatusFilter) : ManageApplicationsEvent
    data class VolunteerClicked(val volunteerId: String) : ManageApplicationsEvent
    data class ApproveClicked(val applicationId: String) : ManageApplicationsEvent
    data object ConfirmOverQuotaApproval : ManageApplicationsEvent
    data object DismissOverQuotaDialog : ManageApplicationsEvent
    data class RejectClicked(val applicationId: String) : ManageApplicationsEvent
    data class RejectionReasonChanged(val reason: String) : ManageApplicationsEvent
    data object ConfirmRejection : ManageApplicationsEvent
    data object DismissRejectDialog : ManageApplicationsEvent
    data object BackClicked : ManageApplicationsEvent
}
