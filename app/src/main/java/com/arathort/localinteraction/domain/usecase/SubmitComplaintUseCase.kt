package com.arathort.localinteraction.domain.usecase

import com.arathort.localinteraction.domain.model.Complaint
import com.arathort.localinteraction.domain.model.ComplaintTargetType
import com.arathort.localinteraction.domain.repository.ComplaintRepository
import javax.inject.Inject

class SubmitComplaintUseCase @Inject constructor(
    private val complaintRepository: ComplaintRepository
) {
    suspend operator fun invoke(
        targetType: ComplaintTargetType,
        targetId: String,
        violationCategory: String,
        description: String
    ): Result<Complaint> {
        val trimmedCategory = violationCategory.trim()
        val trimmedDesc = description.trim()
        require(trimmedCategory.length >= 3) { "Violation category is too short" }
        require(trimmedDesc.length >= 10) { "Description must be at least 10 characters" }
        return complaintRepository.createComplaint(
            targetType = targetType,
            targetId = targetId,
            violationCategory = trimmedCategory,
            description = trimmedDesc
        )
    }
}
