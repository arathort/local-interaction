package com.arathort.localinteraction.presentation.auth

import com.arathort.localinteraction.domain.model.UserRole

sealed interface AuthEvent {
    data class TabSelected(val tab: AuthTab) : AuthEvent
    data class LoginEmailChanged(val email: String) : AuthEvent
    data class LoginPasswordChanged(val password: String) : AuthEvent
    data object ToggleLoginPasswordVisibility : AuthEvent
    data object LoginSubmitted : AuthEvent

    data class RegisterEmailChanged(val email: String) : AuthEvent
    data class RegisterPasswordChanged(val password: String) : AuthEvent
    data object ToggleRegisterPasswordVisibility : AuthEvent
    data class RegisterConfirmPasswordChanged(val password: String) : AuthEvent
    data object ToggleRegisterConfirmPasswordVisibility : AuthEvent
    data class RoleSelected(val role: UserRole) : AuthEvent
    data class PrivacyPolicyConsentChanged(val agreed: Boolean) : AuthEvent
    data object RegisterSubmitted : AuthEvent

    data object BackClicked : AuthEvent
}
