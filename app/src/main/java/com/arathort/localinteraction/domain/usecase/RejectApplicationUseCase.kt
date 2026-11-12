package com.arathort.localinteraction.domain.usecase

import com.arathort.localinteraction.domain.model.VolunteerApplication
import com.arathort.localinteraction.domain.repository.ApplicationRepository
import javax.inject.Inject

class RejectApplicationUseCase @Inject constructor(
    private val applicationRepository: ApplicationRepository
) {
    suspend operator fun invoke(applicationId: String, reason: String): Result<VolunteerApplication> {
        val trimmedReason = reason.trim()
        if (trimmedReason.isEmpty()) {
            return Result.failure(IllegalArgumentException("Rejection reason cannot be blank"))
        }
        return applicationRepository.rejectApplication(applicationId, trimmedReason)
    }
}
