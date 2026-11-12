package com.arathort.localinteraction.presentation.auth

import com.arathort.localinteraction.domain.model.OAuthProvider
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

    data object OpenPasswordRecoveryClicked : AuthEvent
    data object DismissPasswordRecoveryDialog : AuthEvent
    data class RecoveryStepChanged(val step: PasswordRecoveryStep) : AuthEvent
    data class RecoveryEmailChanged(val email: String) : AuthEvent
    data object ForgotPasswordSubmitted : AuthEvent
    data class ResetTokenChanged(val token: String) : AuthEvent
    data class ResetNewPasswordChanged(val password: String) : AuthEvent
    data object ToggleResetNewPasswordVisibility : AuthEvent
    data class ResetConfirmPasswordChanged(val password: String) : AuthEvent
    data object ToggleResetConfirmPasswordVisibility : AuthEvent
    data object ResetPasswordSubmitted : AuthEvent

    data class OAuthProviderClicked(val provider: OAuthProvider) : AuthEvent
    data object DismissOAuthDialog : AuthEvent
    data class OAuthTokenChanged(val token: String) : AuthEvent
    data class OAuthRoleSelected(val role: UserRole) : AuthEvent
    data class OAuthPrivacyConsentChanged(val agreed: Boolean) : AuthEvent
    data object OAuthLoginSubmitted : AuthEvent

    data object BackClicked : AuthEvent
}
