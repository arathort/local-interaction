package com.arathort.localinteraction.domain.usecase

import com.arathort.localinteraction.domain.repository.AuthRepository
import javax.inject.Inject

class ForgotPasswordUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String): Result<Unit> =
        authRepository.forgotPassword(email = email.trim())
}
