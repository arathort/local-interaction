package com.arathort.localinteraction.domain.usecase

import com.arathort.localinteraction.domain.model.User
import com.arathort.localinteraction.domain.model.UserRole
import com.arathort.localinteraction.domain.repository.AuthRepository
import javax.inject.Inject

class RegisterUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        email: String,
        password: String,
        passwordConfirm: String,
        role: UserRole,
        agreedToRules: Boolean
    ): Result<User> {
        if (password != passwordConfirm) {
            return Result.failure(IllegalArgumentException("Паролі не збігаються"))
        }
        return authRepository.register(
            fullName = email.substringBefore("@"),
            email = email,
            password = password,
            role = role,
            acceptedPrivacyPolicy = agreedToRules
        )
    }
}
