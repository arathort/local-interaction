package com.arathort.localinteraction.domain.usecase

import com.arathort.localinteraction.domain.model.User
import com.arathort.localinteraction.domain.model.UserRole
import com.arathort.localinteraction.domain.repository.AuthRepository
import javax.inject.Inject

class SwitchDemoRoleUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(role: UserRole): Result<User> = authRepository.switchDemoRole(role)
}
