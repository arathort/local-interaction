package com.arathort.localinteraction.domain.usecase

import com.arathort.localinteraction.domain.model.OAuthProvider
import com.arathort.localinteraction.domain.model.User
import com.arathort.localinteraction.domain.model.UserRole
import com.arathort.localinteraction.domain.repository.AuthRepository
import javax.inject.Inject

class OAuthLoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        provider: OAuthProvider,
        token: String,
        role: UserRole = UserRole.VOLUNTEER,
        agreedToRules: Boolean = true
    ): Result<User> = authRepository.oauthLogin(
        provider = provider,
        token = token.trim(),
        role = role,
        agreedToRules = agreedToRules
    )
}
