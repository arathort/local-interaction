package com.arathort.localinteraction.domain.usecase

import com.arathort.localinteraction.domain.model.User
import com.arathort.localinteraction.domain.repository.AdminRepository
import javax.inject.Inject

class GetAdminUsersUseCase @Inject constructor(
    private val adminRepository: AdminRepository
) {
    suspend operator fun invoke(role: String? = null, status: String? = null): Result<List<User>> =
        adminRepository.getAdminUsers(role, status)
}
