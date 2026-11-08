package com.arathort.localinteraction.presentation.manage_applications

import androidx.annotation.StringRes

sealed interface ManageApplicationsEffect {
    data class ShowSnackbar(@param:StringRes val messageResId: Int) : ManageApplicationsEffect
    data class NavigateToVolunteerProfile(val volunteerId: String) : ManageApplicationsEffect
    data object NavigateBack : ManageApplicationsEffect
}
