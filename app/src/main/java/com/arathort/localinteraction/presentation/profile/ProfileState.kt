package com.arathort.localinteraction.presentation.profile

import androidx.annotation.StringRes
import com.arathort.localinteraction.domain.model.Event
import com.arathort.localinteraction.domain.model.User

enum class ProfileTab {
    INFO,
    REVIEWS,
    TASKS
}

data class ProfileState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val user: User? = null,
    val isCurrentUser: Boolean = true,
    val isGuest: Boolean = false,
    val selectedTab: ProfileTab = ProfileTab.INFO,
    val organizerEvents: List<Event> = emptyList(),
    val isEditProfileDialogVisible: Boolean = false,
    val editFullName: String = "",
    val editPhone: String = "",
    val editBio: String = "",
    val isSavingProfile: Boolean = false,
    val isUploadingAvatar: Boolean = false,
    val isLogoutDialogVisible: Boolean = false,
    val withdrawConfirmationApplicationId: String? = null,
    val isWithdrawing: Boolean = false,
    val showReportDialog: Boolean = false,
    val isSubmittingReport: Boolean = false,
    @param:StringRes val errorMessageResId: Int? = null
)
