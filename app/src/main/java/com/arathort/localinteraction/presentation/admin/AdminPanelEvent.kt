package com.arathort.localinteraction.presentation.admin

import com.arathort.localinteraction.domain.model.Category
import com.arathort.localinteraction.domain.model.Complaint
import com.arathort.localinteraction.domain.model.Event
import com.arathort.localinteraction.domain.model.User

sealed interface AdminPanelEvent {
    data class SelectTab(val tab: AdminTab) : AdminPanelEvent
    data object Refresh : AdminPanelEvent

    data class ToggleEventDetails(val eventId: String) : AdminPanelEvent
    data class ApproveEvent(val eventId: String) : AdminPanelEvent
    data class OpenRejectEventDialog(val event: Event) : AdminPanelEvent
    data class ConfirmRejectEvent(val reason: String) : AdminPanelEvent
    data object DismissRejectEventDialog : AdminPanelEvent

    data object OpenCreateCategoryDialog : AdminPanelEvent
    data class OpenEditCategoryDialog(val category: Category) : AdminPanelEvent
    data class ConfirmSaveCategory(val name: String) : AdminPanelEvent
    data class OpenDeleteCategoryDialog(val category: Category) : AdminPanelEvent
    data object ConfirmDeleteCategory : AdminPanelEvent
    data object DismissCategoryDialog : AdminPanelEvent

    data class SelectComplaintsUsersSubTab(val subTab: ComplaintsUsersSubTab) : AdminPanelEvent
    data class FilterComplaints(val filter: ComplaintFilter) : AdminPanelEvent
    data class OpenDeleteComplaintContentDialog(val complaint: Complaint) : AdminPanelEvent
    data object ConfirmDeleteComplaintContent : AdminPanelEvent
    data object DismissDeleteComplaintContentDialog : AdminPanelEvent
    data class OpenRejectComplaintDialog(val complaint: Complaint) : AdminPanelEvent
    data class ConfirmRejectComplaint(val note: String?) : AdminPanelEvent
    data object DismissRejectComplaintDialog : AdminPanelEvent

    data class SearchUsers(val query: String) : AdminPanelEvent
    data class FilterUsersByRole(val role: UserRoleFilter) : AdminPanelEvent
    data class FilterUsersByStatus(val status: UserStatusFilter) : AdminPanelEvent
    data class OpenBlockUserDialog(val user: User, val complaintId: String? = null) : AdminPanelEvent
    data class ConfirmBlockUser(val reason: String) : AdminPanelEvent
    data class OpenUnblockUserDialog(val user: User) : AdminPanelEvent
    data object ConfirmUnblockUser : AdminPanelEvent
    data object DismissUserDialog : AdminPanelEvent

    data class ViewUserProfile(val userId: String) : AdminPanelEvent
    data object NavigateBackClicked : AdminPanelEvent
}
