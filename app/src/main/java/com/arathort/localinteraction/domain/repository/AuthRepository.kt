package com.arathort.localinteraction.domain.repository

import com.arathort.localinteraction.domain.model.OAuthProvider
import com.arathort.localinteraction.domain.model.User
import com.arathort.localinteraction.domain.model.UserRole
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUser: Flow<User>

    suspend fun login(email: String, password: String): Result<User>

    suspend fun register(
        fullName: String,
        email: String,
        password: String,
        role: UserRole,
        acceptedPrivacyPolicy: Boolean
    ): Result<User>

    suspend fun oauthLogin(
        provider: OAuthProvider,
        token: String,
        role: UserRole = UserRole.VOLUNTEER,
        agreedToRules: Boolean = true
    ): Result<User> = Result.failure(UnsupportedOperationException())

    suspend fun forgotPassword(email: String): Result<Unit> = Result.success(Unit)

    suspend fun resetPassword(
        resetToken: String,
        newPassword: String,
        newPasswordConfirm: String
    ): Result<Unit> = Result.success(Unit)

    suspend fun logout(): Result<Unit>

    suspend fun restoreSession(): Result<User?>
}
