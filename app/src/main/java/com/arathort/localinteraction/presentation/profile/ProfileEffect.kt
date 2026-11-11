package com.arathort.localinteraction.presentation.profile

import androidx.annotation.StringRes

sealed interface ProfileEffect {
    data class ShowSnackbar(@param:StringRes val messageResId: Int) : ProfileEffect
    data class ShowSnackbarText(val message: String) : ProfileEffect
    data object NavigateToAuth : ProfileEffect
    data class NavigateToEventDetails(val eventId: String) : ProfileEffect
    data class NavigateToReport(val targetType: String, val targetId: String, val targetTitle: String) : ProfileEffect
    data object NavigateBack : ProfileEffect
}
