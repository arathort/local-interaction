package com.arathort.localinteraction.presentation.auth

import androidx.annotation.StringRes
import com.arathort.localinteraction.domain.model.OAuthProvider
import com.arathort.localinteraction.domain.model.UserRole

enum class AuthTab {
    LOGIN,
    REGISTER
}

enum class PasswordRecoveryStep {
    REQUEST_EMAIL,
    CONFIRM_RESET
}

data class AuthState(
    val currentTab: AuthTab = AuthTab.LOGIN,
    val loginEmail: String = "",
    @param:StringRes val loginEmailErrorResId: Int? = null,
    val loginPassword: String = "",
    @param:StringRes val loginPasswordErrorResId: Int? = null,
    val isLoginPasswordVisible: Boolean = false,
    val registerEmail: String = "",
    @param:StringRes val registerEmailErrorResId: Int? = null,
    val registerPassword: String = "",
    @param:StringRes val registerPasswordErrorResId: Int? = null,
    val isRegisterPasswordVisible: Boolean = false,
    val registerConfirmPassword: String = "",
    @param:StringRes val registerConfirmPasswordErrorResId: Int? = null,
    val isRegisterConfirmPasswordVisible: Boolean = false,
    val selectedRole: UserRole? = null,
    @param:StringRes val roleErrorResId: Int? = null,
    val acceptedPrivacyPolicy: Boolean = false,
    @param:StringRes val privacyPolicyErrorResId: Int? = null,
    val isLoading: Boolean = false,
    val showPasswordRecoveryDialog: Boolean = false,
    val recoveryStep: PasswordRecoveryStep = PasswordRecoveryStep.REQUEST_EMAIL,
    val recoveryEmail: String = "",
    @param:StringRes val recoveryEmailErrorResId: Int? = null,
    val resetToken: String = "",
    @param:StringRes val resetTokenErrorResId: Int? = null,
    val resetNewPassword: String = "",
    @param:StringRes val resetNewPasswordErrorResId: Int? = null,
    val isResetNewPasswordVisible: Boolean = false,
    val resetConfirmPassword: String = "",
    @param:StringRes val resetConfirmPasswordErrorResId: Int? = null,
    val isResetConfirmPasswordVisible: Boolean = false,
    val isRecoveryLoading: Boolean = false,
    val activeOAuthProvider: OAuthProvider? = null,
    val oauthTokenInput: String = "",
    @param:StringRes val oauthTokenErrorResId: Int? = null,
    val oauthSelectedRole: UserRole = UserRole.VOLUNTEER,
    val oauthAcceptedPrivacyPolicy: Boolean = true,
    @param:StringRes val oauthPrivacyErrorResId: Int? = null,
    val isOAuthLoading: Boolean = false
)
