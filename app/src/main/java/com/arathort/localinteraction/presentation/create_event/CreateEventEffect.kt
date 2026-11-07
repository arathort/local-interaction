package com.arathort.localinteraction.presentation.create_event

import androidx.annotation.StringRes

sealed interface CreateEventEffect {
    data class ShowSnackbar(@param:StringRes val messageResId: Int) : CreateEventEffect
    data class NavigateToEventDetails(val eventId: String) : CreateEventEffect
    data object NavigateBack : CreateEventEffect
}
