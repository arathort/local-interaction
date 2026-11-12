package com.arathort.localinteraction.domain.usecase

import com.arathort.localinteraction.domain.model.VolunteerApplication
import com.arathort.localinteraction.domain.repository.ApplicationRepository
import javax.inject.Inject

class ApproveApplicationUseCase @Inject constructor(
    private val applicationRepository: ApplicationRepository
) {
    suspend operator fun invoke(applicationId: String): Result<VolunteerApplication> =
        applicationRepository.approveApplication(applicationId)
}
