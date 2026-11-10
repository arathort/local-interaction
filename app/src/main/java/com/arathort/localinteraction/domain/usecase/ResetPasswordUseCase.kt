package com.arathort.localinteraction.domain.usecase

import com.arathort.localinteraction.domain.repository.AuthRepository
import javax.inject.Inject

class ResetPasswordUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        resetToken: String,
        newPassword: String,
        newPasswordConfirm: String
    ): Result<Unit> = authRepository.resetPassword(
        resetToken = resetToken.trim(),
        newPassword = newPassword,
        newPasswordConfirm = newPasswordConfirm
    )
}
