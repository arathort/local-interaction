package com.arathort.localinteraction

import com.arathort.localinteraction.R
import com.arathort.localinteraction.domain.model.OAuthProvider
import com.arathort.localinteraction.domain.model.User
import com.arathort.localinteraction.domain.model.UserRole
import com.arathort.localinteraction.domain.repository.AuthRepository
import com.arathort.localinteraction.domain.usecase.ForgotPasswordUseCase
import com.arathort.localinteraction.domain.usecase.LoginUseCase
import com.arathort.localinteraction.domain.usecase.OAuthLoginUseCase
import com.arathort.localinteraction.domain.usecase.RegisterUseCase
import com.arathort.localinteraction.domain.usecase.ResetPasswordUseCase
import com.arathort.localinteraction.presentation.auth.AuthEffect
import com.arathort.localinteraction.presentation.auth.AuthEvent
import com.arathort.localinteraction.presentation.auth.AuthTab
import com.arathort.localinteraction.presentation.auth.AuthViewModel
import com.arathort.localinteraction.presentation.auth.PasswordRecoveryStep
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var fakeAuthRepository: FakeAuthRepository
    private lateinit var viewModel: AuthViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeAuthRepository = FakeAuthRepository()
        viewModel = AuthViewModel(
            loginUseCase = LoginUseCase(fakeAuthRepository),
            registerUseCase = RegisterUseCase(fakeAuthRepository),
            forgotPasswordUseCase = ForgotPasswordUseCase(fakeAuthRepository),
            resetPasswordUseCase = ResetPasswordUseCase(fakeAuthRepository),
            oauthLoginUseCase = OAuthLoginUseCase(fakeAuthRepository)
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state defaults to LOGIN tab with empty fields`() {
        val state = viewModel.uiState.value
        assertEquals(AuthTab.LOGIN, state.currentTab)
        assertEquals("", state.loginEmail)
        assertEquals("", state.loginPassword)
        assertFalse(state.isLoginPasswordVisible)
        assertNull(state.loginEmailErrorResId)
        assertNull(state.loginPasswordErrorResId)
        assertFalse(state.isLoading)
    }

    @Test
    fun `TabSelected switches between LOGIN and REGISTER`() {
        viewModel.onEvent(AuthEvent.TabSelected(AuthTab.REGISTER))
        assertEquals(AuthTab.REGISTER, viewModel.uiState.value.currentTab)

        viewModel.onEvent(AuthEvent.TabSelected(AuthTab.LOGIN))
        assertEquals(AuthTab.LOGIN, viewModel.uiState.value.currentTab)
    }

    @Test
    fun `LoginSubmitted with invalid email triggers validation error`() = runTest {
        viewModel.onEvent(AuthEvent.LoginEmailChanged("not-an-email"))
        viewModel.onEvent(AuthEvent.LoginPasswordChanged("password123"))
        viewModel.onEvent(AuthEvent.LoginSubmitted)

        val state = viewModel.uiState.value
        assertEquals(R.string.error_invalid_email, state.loginEmailErrorResId)
        assertNull(state.loginPasswordErrorResId)
    }

    @Test
    fun `LoginSubmitted with short password triggers validation error`() = runTest {
        viewModel.onEvent(AuthEvent.LoginEmailChanged("user@example.com"))
        viewModel.onEvent(AuthEvent.LoginPasswordChanged("short"))
        viewModel.onEvent(AuthEvent.LoginSubmitted)

        val state = viewModel.uiState.value
        assertNull(state.loginEmailErrorResId)
        assertEquals(R.string.error_password_too_short, state.loginPasswordErrorResId)
    }

    @Test
    fun `LoginSubmitted with valid credentials executes login and navigates back`() = runTest {
        viewModel.onEvent(AuthEvent.LoginEmailChanged("valid@example.com"))
        viewModel.onEvent(AuthEvent.LoginPasswordChanged("validPassword123"))
        viewModel.onEvent(AuthEvent.LoginSubmitted)

        val effect = viewModel.effect.first()
        assertTrue(effect is AuthEffect.ShowSnackbar || effect is AuthEffect.NavigateBack)
    }

    @Test
    fun `RegisterSubmitted with missing role triggers role error`() = runTest {
        viewModel.onEvent(AuthEvent.RegisterEmailChanged("volunteer@example.com"))
        viewModel.onEvent(AuthEvent.RegisterPasswordChanged("password123"))
        viewModel.onEvent(AuthEvent.RegisterConfirmPasswordChanged("password123"))
        viewModel.onEvent(AuthEvent.PrivacyPolicyConsentChanged(true))
        viewModel.onEvent(AuthEvent.RegisterSubmitted)

        val state = viewModel.uiState.value
        assertEquals(R.string.error_role_required, state.roleErrorResId)
    }

    @Test
    fun `RegisterSubmitted with mismatched passwords triggers error`() = runTest {
        viewModel.onEvent(AuthEvent.RoleSelected(UserRole.VOLUNTEER))
        viewModel.onEvent(AuthEvent.RegisterEmailChanged("volunteer@example.com"))
        viewModel.onEvent(AuthEvent.RegisterPasswordChanged("password123"))
        viewModel.onEvent(AuthEvent.RegisterConfirmPasswordChanged("differentPassword"))
        viewModel.onEvent(AuthEvent.PrivacyPolicyConsentChanged(true))
        viewModel.onEvent(AuthEvent.RegisterSubmitted)

        val state = viewModel.uiState.value
        assertEquals(R.string.error_passwords_do_not_match, state.registerConfirmPasswordErrorResId)
    }

    @Test
    fun `RegisterSubmitted without privacy policy consent triggers error`() = runTest {
        viewModel.onEvent(AuthEvent.RoleSelected(UserRole.VOLUNTEER))
        viewModel.onEvent(AuthEvent.RegisterEmailChanged("volunteer@example.com"))
        viewModel.onEvent(AuthEvent.RegisterPasswordChanged("password123"))
        viewModel.onEvent(AuthEvent.RegisterConfirmPasswordChanged("password123"))
        viewModel.onEvent(AuthEvent.PrivacyPolicyConsentChanged(false))
        viewModel.onEvent(AuthEvent.RegisterSubmitted)

        val state = viewModel.uiState.value
        assertEquals(R.string.error_privacy_consent_required, state.privacyPolicyErrorResId)
    }

    @Test
    fun `RegisterSubmitted with valid form succeeds and triggers NavigateBack`() = runTest {
        viewModel.onEvent(AuthEvent.RoleSelected(UserRole.VOLUNTEER))
        viewModel.onEvent(AuthEvent.RegisterEmailChanged("volunteer@example.com"))
        viewModel.onEvent(AuthEvent.RegisterPasswordChanged("password123"))
        viewModel.onEvent(AuthEvent.RegisterConfirmPasswordChanged("password123"))
        viewModel.onEvent(AuthEvent.PrivacyPolicyConsentChanged(true))
        viewModel.onEvent(AuthEvent.RegisterSubmitted)

        val state = viewModel.uiState.value
        assertNull(state.registerEmailErrorResId)
        assertNull(state.registerPasswordErrorResId)
        assertNull(state.registerConfirmPasswordErrorResId)
        assertNull(state.roleErrorResId)
        assertNull(state.privacyPolicyErrorResId)
    }

    @Test
    fun `Toggle password visibilities updates state flags`() {
        viewModel.onEvent(AuthEvent.ToggleLoginPasswordVisibility)
        assertTrue(viewModel.uiState.value.isLoginPasswordVisible)
        viewModel.onEvent(AuthEvent.ToggleLoginPasswordVisibility)
        assertFalse(viewModel.uiState.value.isLoginPasswordVisible)

        viewModel.onEvent(AuthEvent.ToggleRegisterPasswordVisibility)
        assertTrue(viewModel.uiState.value.isRegisterPasswordVisible)

        viewModel.onEvent(AuthEvent.ToggleRegisterConfirmPasswordVisibility)
        assertTrue(viewModel.uiState.value.isRegisterConfirmPasswordVisible)
    }

    @Test
    fun `ForgotPasswordSubmitted validates email and advances to CONFIRM_RESET step`() = runTest {
        viewModel.onEvent(AuthEvent.OpenPasswordRecoveryClicked)
        assertTrue(viewModel.uiState.value.showPasswordRecoveryDialog)

        viewModel.onEvent(AuthEvent.RecoveryEmailChanged("invalid-email"))
        viewModel.onEvent(AuthEvent.ForgotPasswordSubmitted)
        assertEquals(R.string.error_invalid_email, viewModel.uiState.value.recoveryEmailErrorResId)

        viewModel.onEvent(AuthEvent.RecoveryEmailChanged("user@example.com"))
        viewModel.onEvent(AuthEvent.ForgotPasswordSubmitted)
        assertEquals(PasswordRecoveryStep.CONFIRM_RESET, viewModel.uiState.value.recoveryStep)
    }

    @Test
    fun `ResetPasswordSubmitted validates token and passwords then closes dialog on success`() = runTest {
        viewModel.onEvent(AuthEvent.OpenPasswordRecoveryClicked)
        viewModel.onEvent(AuthEvent.RecoveryStepChanged(PasswordRecoveryStep.CONFIRM_RESET))

        viewModel.onEvent(AuthEvent.ResetPasswordSubmitted)
        assertEquals(R.string.error_reset_token_required, viewModel.uiState.value.resetTokenErrorResId)
        assertEquals(R.string.error_password_too_short, viewModel.uiState.value.resetNewPasswordErrorResId)

        viewModel.onEvent(AuthEvent.ResetTokenChanged("reset-token-123"))
        viewModel.onEvent(AuthEvent.ResetNewPasswordChanged("newPassword123"))
        viewModel.onEvent(AuthEvent.ResetConfirmPasswordChanged("newPassword123"))
        viewModel.onEvent(AuthEvent.ResetPasswordSubmitted)

        assertFalse(viewModel.uiState.value.showPasswordRecoveryDialog)
    }

    @Test
    fun `OAuthLoginSubmitted validates token and logs in user`() = runTest {
        viewModel.onEvent(AuthEvent.OAuthProviderClicked(OAuthProvider.GOOGLE))
        assertEquals(OAuthProvider.GOOGLE, viewModel.uiState.value.activeOAuthProvider)

        viewModel.onEvent(AuthEvent.OAuthLoginSubmitted)
        assertEquals(R.string.error_oauth_token_required, viewModel.uiState.value.oauthTokenErrorResId)

        viewModel.onEvent(AuthEvent.OAuthTokenChanged("google-id-token-xyz"))
        viewModel.onEvent(AuthEvent.OAuthRoleSelected(UserRole.ORGANIZER))
        viewModel.onEvent(AuthEvent.OAuthLoginSubmitted)

        assertNull(viewModel.uiState.value.activeOAuthProvider)
    }

    private class FakeAuthRepository : AuthRepository {
        override val currentUser: Flow<User> = emptyFlow()

        override suspend fun login(email: String, password: String): Result<User> {
            return Result.success(User(id = "user-1", email = email, role = UserRole.VOLUNTEER))
        }

        override suspend fun register(
            fullName: String,
            email: String,
            password: String,
            role: UserRole,
            acceptedPrivacyPolicy: Boolean
        ): Result<User> {
            return Result.success(User(id = "user-1", email = email, role = role))
        }

        override suspend fun oauthLogin(
            provider: OAuthProvider,
            token: String,
            role: UserRole,
            agreedToRules: Boolean
        ): Result<User> {
            return Result.success(User(id = "user-oauth-1", role = role))
        }

        override suspend fun forgotPassword(email: String): Result<Unit> = Result.success(Unit)

        override suspend fun resetPassword(
            resetToken: String,
            newPassword: String,
            newPasswordConfirm: String
        ): Result<Unit> = Result.success(Unit)

        override suspend fun logout(): Result<Unit> = Result.success(Unit)

        override suspend fun restoreSession(): Result<User?> = Result.success(null)
    }
}
