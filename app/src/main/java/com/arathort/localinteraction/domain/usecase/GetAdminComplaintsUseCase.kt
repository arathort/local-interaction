package com.arathort.localinteraction.domain.usecase

import com.arathort.localinteraction.domain.model.Complaint
import com.arathort.localinteraction.domain.repository.AdminRepository
import javax.inject.Inject

class GetAdminComplaintsUseCase @Inject constructor(
    private val adminRepository: AdminRepository
) {
    suspend operator fun invoke(statusFilter: String? = null): Result<List<Complaint>> =
        adminRepository.getAdminComplaints(statusFilter)
}
