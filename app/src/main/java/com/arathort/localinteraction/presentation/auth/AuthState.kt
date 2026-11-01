package com.arathort.localinteraction.presentation.auth

import androidx.annotation.StringRes
import com.arathort.localinteraction.domain.model.UserRole

enum class AuthTab {
    LOGIN,
    REGISTER
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
    val isLoading: Boolean = false
)
