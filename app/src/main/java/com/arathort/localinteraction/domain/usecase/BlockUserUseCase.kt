package com.arathort.localinteraction.domain.usecase

import com.arathort.localinteraction.domain.model.User
import com.arathort.localinteraction.domain.repository.AdminRepository
import javax.inject.Inject

class BlockUserUseCase @Inject constructor(
    private val adminRepository: AdminRepository
) {
    suspend operator fun invoke(userId: String, reason: String, complaintId: String? = null): Result<User> =
        adminRepository.blockUser(userId, reason, complaintId)
}
