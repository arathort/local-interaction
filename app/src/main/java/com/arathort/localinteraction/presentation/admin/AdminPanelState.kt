package com.arathort.localinteraction.presentation.admin

import com.arathort.localinteraction.domain.model.Category
import com.arathort.localinteraction.domain.model.Complaint
import com.arathort.localinteraction.domain.model.Event
import com.arathort.localinteraction.domain.model.User

enum class AdminTab {
    MODERATION,
    CATEGORIES,
    COMPLAINTS_AND_USERS
}

enum class ComplaintsUsersSubTab {
    COMPLAINTS,
    USERS
}

enum class ComplaintFilter(val apiValue: String?) {
    ALL(null),
    PENDING("Pending"),
    REJECTED("Rejected"),
    RESOLVED("Resolved")
}

enum class UserRoleFilter(val apiValue: String?) {
    ALL(null),
    VOLUNTEER("Volunteer"),
    ORGANIZER("Organizer"),
    ADMIN("Admin")
}

enum class UserStatusFilter(val apiValue: String?) {
    ALL(null),
    ACTIVE("Active"),
    BLOCKED("Blocked")
}

sealed interface CategoryDialogMode {
    data object Create : CategoryDialogMode
    data class Edit(val category: Category) : CategoryDialogMode
}

data class AdminPanelState(
    val selectedTab: AdminTab = AdminTab.MODERATION,
    val isLoading: Boolean = false,
    val isActionLoading: Boolean = false,
    val errorMessage: String? = null,
    val isAuthorizedAdmin: Boolean = false,

    val pendingEvents: List<Event> = emptyList(),
    val expandedEventId: String? = null,
    val eventToReject: Event? = null,

    val categories: List<Category> = emptyList(),
    val categoryDialogMode: CategoryDialogMode? = null,
    val categoryToDelete: Category? = null,

    val complaintsUsersSubTab: ComplaintsUsersSubTab = ComplaintsUsersSubTab.COMPLAINTS,
    val complaints: List<Complaint> = emptyList(),
    val complaintFilter: ComplaintFilter = ComplaintFilter.ALL,
    val complaintToReject: Complaint? = null,
    val complaintToDeleteContent: Complaint? = null,

    val users: List<User> = emptyList(),
    val userSearchQuery: String = "",
    val userRoleFilter: UserRoleFilter = UserRoleFilter.ALL,
    val userStatusFilter: UserStatusFilter = UserStatusFilter.ALL,
    val userToBlock: User? = null,
    val userToUnblock: User? = null,
    val blockComplaintId: String? = null
) {
    val filteredUsers: List<User>
        get() {
            return users.filter { user ->
                val matchesQuery = userSearchQuery.isBlank() ||
                    (user.fullName?.contains(userSearchQuery, ignoreCase = true) == true) ||
                    user.email.contains(userSearchQuery, ignoreCase = true)

                val matchesRole = when (userRoleFilter) {
                    UserRoleFilter.ALL -> true
                    UserRoleFilter.VOLUNTEER -> user.role == com.arathort.localinteraction.domain.model.UserRole.VOLUNTEER
                    UserRoleFilter.ORGANIZER -> user.role == com.arathort.localinteraction.domain.model.UserRole.ORGANIZER
                    UserRoleFilter.ADMIN -> user.role == com.arathort.localinteraction.domain.model.UserRole.ADMIN
                }

                val matchesStatus = when (userStatusFilter) {
                    UserStatusFilter.ALL -> true
                    UserStatusFilter.ACTIVE -> user.status == com.arathort.localinteraction.domain.model.UserStatus.ACTIVE
                    UserStatusFilter.BLOCKED -> user.status == com.arathort.localinteraction.domain.model.UserStatus.BLOCKED
                }

                matchesQuery && matchesRole && matchesStatus
            }
        }
}
