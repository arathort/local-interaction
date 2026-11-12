package com.arathort.localinteraction.domain.usecase

import com.arathort.localinteraction.domain.model.User
import com.arathort.localinteraction.domain.repository.AdminRepository
import javax.inject.Inject

class UnblockUserUseCase @Inject constructor(
    private val adminRepository: AdminRepository
) {
    suspend operator fun invoke(userId: String): Result<User> =
        adminRepository.unblockUser(userId)
}
