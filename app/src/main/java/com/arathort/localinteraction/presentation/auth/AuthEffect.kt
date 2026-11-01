package com.arathort.localinteraction.presentation.auth

import androidx.annotation.StringRes

sealed interface AuthEffect {
    data object NavigateBack : AuthEffect
    data class ShowSnackbar(@param:StringRes val messageResId: Int) : AuthEffect
    data class ShowSnackbarText(val message: String) : AuthEffect
}
