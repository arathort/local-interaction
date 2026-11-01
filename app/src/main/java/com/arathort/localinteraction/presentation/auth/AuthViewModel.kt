package com.arathort.localinteraction.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arathort.localinteraction.R
import com.arathort.localinteraction.domain.model.UserRole
import com.arathort.localinteraction.domain.usecase.LoginUseCase
import com.arathort.localinteraction.domain.usecase.RegisterUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthState())
    val uiState: StateFlow<AuthState> = _uiState.asStateFlow()

    private val _effect = Channel<AuthEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    fun onEvent(event: AuthEvent) {
        when (event) {
            is AuthEvent.TabSelected -> _uiState.update { it.copy(currentTab = event.tab) }
            is AuthEvent.LoginEmailChanged -> _uiState.update {
                it.copy(loginEmail = event.email, loginEmailErrorResId = null)
            }
            is AuthEvent.LoginPasswordChanged -> _uiState.update {
                it.copy(loginPassword = event.password, loginPasswordErrorResId = null)
            }
            AuthEvent.ToggleLoginPasswordVisibility -> _uiState.update {
                it.copy(isLoginPasswordVisible = !it.isLoginPasswordVisible)
            }
            AuthEvent.LoginSubmitted -> handleLoginSubmit()

            is AuthEvent.RegisterEmailChanged -> _uiState.update {
                it.copy(registerEmail = event.email, registerEmailErrorResId = null)
            }
            is AuthEvent.RegisterPasswordChanged -> _uiState.update {
                it.copy(registerPassword = event.password, registerPasswordErrorResId = null)
            }
            AuthEvent.ToggleRegisterPasswordVisibility -> _uiState.update {
                it.copy(isRegisterPasswordVisible = !it.isRegisterPasswordVisible)
            }
            is AuthEvent.RegisterConfirmPasswordChanged -> _uiState.update {
                it.copy(registerConfirmPassword = event.password, registerConfirmPasswordErrorResId = null)
            }
            AuthEvent.ToggleRegisterConfirmPasswordVisibility -> _uiState.update {
                it.copy(isRegisterConfirmPasswordVisible = !it.isRegisterConfirmPasswordVisible)
            }
            is AuthEvent.RoleSelected -> _uiState.update {
                it.copy(selectedRole = event.role, roleErrorResId = null)
            }
            is AuthEvent.PrivacyPolicyConsentChanged -> _uiState.update {
                it.copy(acceptedPrivacyPolicy = event.agreed, privacyPolicyErrorResId = null)
            }
            AuthEvent.RegisterSubmitted -> handleRegisterSubmit()

            AuthEvent.BackClicked -> {
                viewModelScope.launch {
                    _effect.send(AuthEffect.NavigateBack)
                }
            }
        }
    }

    private fun handleLoginSubmit() {
        val state = _uiState.value
        val email = state.loginEmail.trim()
        val password = state.loginPassword

        var hasError = false
        var emailError: Int? = null
        var passwordError: Int? = null

        if (!isValidEmail(email)) {
            emailError = R.string.error_invalid_email
            hasError = true
        }
        if (password.length < 8) {
            passwordError = R.string.error_password_too_short
            hasError = true
        }

        if (hasError) {
            _uiState.update {
                it.copy(
                    loginEmailErrorResId = emailError,
                    loginPasswordErrorResId = passwordError
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            loginUseCase(email = email, password = password)
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false) }
                    _effect.send(AuthEffect.ShowSnackbar(R.string.auth_login_success))
                    _effect.send(AuthEffect.NavigateBack)
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false) }
                    val message = error.message.orEmpty()
                    if (message.isNotBlank()) {
                        _effect.send(AuthEffect.ShowSnackbarText(message))
                    } else {
                        _effect.send(AuthEffect.ShowSnackbar(R.string.error_auth_invalid_credentials))
                    }
                }
        }
    }

    private fun handleRegisterSubmit() {
        val state = _uiState.value
        val email = state.registerEmail.trim()
        val password = state.registerPassword
        val passwordConfirm = state.registerConfirmPassword
        val role = state.selectedRole
        val agreedToRules = state.acceptedPrivacyPolicy

        var hasError = false
        var emailError: Int? = null
        var passwordError: Int? = null
        var confirmError: Int? = null
        var roleError: Int? = null
        var privacyError: Int? = null

        if (role == null || role == UserRole.GUEST || role == UserRole.ADMIN) {
            roleError = R.string.error_role_required
            hasError = true
        }
        if (!isValidEmail(email)) {
            emailError = R.string.error_invalid_email
            hasError = true
        }
        if (password.length < 8) {
            passwordError = R.string.error_password_too_short
            hasError = true
        }
        if (password != passwordConfirm) {
            confirmError = R.string.error_passwords_do_not_match
            hasError = true
        }
        if (!agreedToRules) {
            privacyError = R.string.error_privacy_consent_required
            hasError = true
        }

        if (hasError || role == null) {
            _uiState.update {
                it.copy(
                    registerEmailErrorResId = emailError,
                    registerPasswordErrorResId = passwordError,
                    registerConfirmPasswordErrorResId = confirmError,
                    roleErrorResId = roleError,
                    privacyPolicyErrorResId = privacyError
                )
            }
            return
        }

        val targetRole = role
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            registerUseCase(
                email = email,
                password = password,
                passwordConfirm = passwordConfirm,
                role = targetRole,
                agreedToRules = agreedToRules
            )
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false) }
                    _effect.send(AuthEffect.ShowSnackbar(R.string.auth_register_success))
                    _effect.send(AuthEffect.NavigateBack)
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false) }
                    val message = error.message.orEmpty()
                    if (message.isNotBlank()) {
                        _effect.send(AuthEffect.ShowSnackbarText(message))
                    } else {
                        _effect.send(AuthEffect.ShowSnackbar(R.string.error_unknown))
                    }
                }
        }
    }

    private fun isValidEmail(email: String): Boolean =
        email.isNotBlank() && email.contains("@") && email.contains(".") && !email.contains(" ")
}
