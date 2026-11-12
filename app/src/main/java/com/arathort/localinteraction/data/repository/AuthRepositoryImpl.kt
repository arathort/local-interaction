package com.arathort.localinteraction.data.repository

import com.arathort.localinteraction.data.datasource.local.TokenStorage
import com.arathort.localinteraction.data.datasource.remote.JwtTokenManager
import com.arathort.localinteraction.data.datasource.remote.LocalInteractionApiService
import com.arathort.localinteraction.data.dto.ForgotPasswordRequestDto
import com.arathort.localinteraction.data.dto.OAuthLoginRequestDto
import com.arathort.localinteraction.data.dto.ResetPasswordConfirmRequestDto
import com.arathort.localinteraction.data.dto.UserCreateDto
import com.arathort.localinteraction.data.mapper.toDomain
import com.arathort.localinteraction.domain.model.OAuthProvider
import com.arathort.localinteraction.domain.model.User
import com.arathort.localinteraction.domain.model.UserRole
import com.arathort.localinteraction.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val apiService: LocalInteractionApiService,
    private val tokenStorage: TokenStorage,
    private val jwtTokenManager: JwtTokenManager
) : AuthRepository {

    private val guestUser = User(id = "user-guest", role = UserRole.GUEST)
    private val _currentUser = MutableStateFlow(guestUser)
    override val currentUser: Flow<User> = _currentUser.asStateFlow()

    override suspend fun login(email: String, password: String): Result<User> = runCatching {
        val trimmedEmail = email.trim()
        val tokenDto = apiService.login(username = trimmedEmail, password = password)
        tokenStorage.saveAccessToken(tokenDto.accessToken)
        jwtTokenManager.updateToken(tokenDto.accessToken)
        val profile = apiService.getMyProfile().toDomain()
        _currentUser.value = profile
        profile
    }

    override suspend fun register(
        fullName: String,
        email: String,
        password: String,
        role: UserRole,
        acceptedPrivacyPolicy: Boolean
    ): Result<User> = runCatching {
        val trimmedEmail = email.trim()
        val request = UserCreateDto(
            email = trimmedEmail,
            password = password,
            passwordConfirm = password,
            role = role.apiValue,
            agreedToRules = acceptedPrivacyPolicy
        )
        apiService.register(request)
        login(trimmedEmail, password).getOrThrow()
    }

    override suspend fun oauthLogin(
        provider: OAuthProvider,
        token: String,
        role: UserRole,
        agreedToRules: Boolean
    ): Result<User> = runCatching {
        val request = OAuthLoginRequestDto(
            token = token.trim(),
            role = role.apiValue,
            agreedToRules = agreedToRules
        )
        val tokenDto = apiService.oauthLogin(
            provider = provider.apiValue,
            request = request
        )
        tokenStorage.saveAccessToken(tokenDto.accessToken)
        jwtTokenManager.updateToken(tokenDto.accessToken)
        val profile = apiService.getMyProfile().toDomain()
        _currentUser.value = profile
        profile
    }

    override suspend fun forgotPassword(email: String): Result<Unit> = runCatching {
        apiService.forgotPassword(ForgotPasswordRequestDto(email = email.trim()))
    }

    override suspend fun resetPassword(
        resetToken: String,
        newPassword: String,
        newPasswordConfirm: String
    ): Result<Unit> = runCatching {
        apiService.resetPassword(
            ResetPasswordConfirmRequestDto(
                resetToken = resetToken.trim(),
                newPassword = newPassword,
                newPasswordConfirm = newPasswordConfirm
            )
        )
    }

    override suspend fun logout(): Result<Unit> = runCatching {
        runCatching { apiService.logout() }
        tokenStorage.clearAccessToken()
        jwtTokenManager.clearToken()
        _currentUser.value = guestUser
    }

    override suspend fun restoreSession(): Result<User?> = runCatching {
        val savedToken = tokenStorage.accessTokenFlow.firstOrNull()
        if (!savedToken.isNullOrBlank()) {
            jwtTokenManager.updateToken(savedToken)
            runCatching {
                val profile = apiService.getMyProfile().toDomain()
                _currentUser.value = profile
                profile
            }.getOrElse {
                tokenStorage.clearAccessToken()
                jwtTokenManager.clearToken()
                _currentUser.value = guestUser
                null
            }
        } else {
            null
        }
    }
}
