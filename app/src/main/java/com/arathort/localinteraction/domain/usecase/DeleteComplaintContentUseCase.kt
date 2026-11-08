package com.arathort.localinteraction.domain.usecase

import com.arathort.localinteraction.domain.model.Complaint
import com.arathort.localinteraction.domain.repository.AdminRepository
import javax.inject.Inject

class DeleteComplaintContentUseCase @Inject constructor(
    private val adminRepository: AdminRepository
) {
    suspend operator fun invoke(complaintId: String): Result<Complaint> =
        adminRepository.deleteComplaintContent(complaintId)
}
