package com.arathort.localinteraction.presentation.manage_applications

import androidx.annotation.StringRes
import com.arathort.localinteraction.domain.model.ApplicationStatus
import com.arathort.localinteraction.domain.model.EventStatus
import com.arathort.localinteraction.domain.model.VolunteerApplication

data class ManageApplicationsState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isProcessingAction: Boolean = false,
    val isOrganizer: Boolean = true,
    val eventId: String = "",
    val eventTitle: String = "",
    val eventStatus: EventStatus = EventStatus.OPEN,
    val volunteerQuota: Int = 0,
    val approvedVolunteersCount: Int = 0,
    val applications: List<VolunteerApplication> = emptyList(),
    val selectedFilter: ApplicationStatusFilter = ApplicationStatusFilter.ALL,
    val rejectingApplicationId: String? = null,
    val rejectionReasonText: String = "",
    @param:StringRes val rejectionReasonErrorResId: Int? = null,
    val pendingApprovalApplicationId: String? = null,
    @param:StringRes val errorMessageResId: Int? = null
) {
    val filteredApplications: List<VolunteerApplication>
        get() = applications.filter { selectedFilter.matches(it.status) }

    val pendingCount: Int
        get() = applications.count { it.status == ApplicationStatus.PENDING }

    val approvedCount: Int
        get() = applications.count { it.status == ApplicationStatus.APPROVED }

    val rejectedCount: Int
        get() = applications.count { it.status == ApplicationStatus.REJECTED }

    val withdrawnCount: Int
        get() = applications.count { it.status == ApplicationStatus.WITHDRAWN }
}
