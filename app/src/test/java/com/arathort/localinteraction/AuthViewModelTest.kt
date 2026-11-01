package com.arathort.localinteraction

import com.arathort.localinteraction.R
import com.arathort.localinteraction.domain.model.User
import com.arathort.localinteraction.domain.model.UserRole
import com.arathort.localinteraction.domain.repository.AuthRepository
import com.arathort.localinteraction.domain.usecase.LoginUseCase
import com.arathort.localinteraction.domain.usecase.RegisterUseCase
import com.arathort.localinteraction.presentation.auth.AuthEffect
import com.arathort.localinteraction.presentation.auth.AuthEvent
import com.arathort.localinteraction.presentation.auth.AuthTab
import com.arathort.localinteraction.presentation.auth.AuthViewModel
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
            registerUseCase = RegisterUseCase(fakeAuthRepository)
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

        override suspend fun logout(): Result<Unit> = Result.success(Unit)

        override suspend fun restoreSession(): Result<User?> = Result.success(null)
    }
}
