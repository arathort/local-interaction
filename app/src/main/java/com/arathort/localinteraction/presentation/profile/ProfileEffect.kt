package com.arathort.localinteraction.presentation.profile

import androidx.annotation.StringRes

sealed interface ProfileEffect {
    data class ShowSnackbar(@param:StringRes val messageResId: Int) : ProfileEffect
    data object NavigateToAuth : ProfileEffect
    data class NavigateToEventDetails(val eventId: String) : ProfileEffect
    data object NavigateBack : ProfileEffect
}
