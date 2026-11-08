package com.arathort.localinteraction.domain.usecase

import com.arathort.localinteraction.domain.model.Event
import com.arathort.localinteraction.domain.repository.AdminRepository
import javax.inject.Inject

class ApproveEventModerationUseCase @Inject constructor(
    private val adminRepository: AdminRepository
) {
    suspend operator fun invoke(eventId: String): Result<Event> =
        adminRepository.approveEventModeration(eventId)
}
