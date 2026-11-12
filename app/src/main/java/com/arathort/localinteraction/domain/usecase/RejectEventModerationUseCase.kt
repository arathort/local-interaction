package com.arathort.localinteraction.domain.usecase

import com.arathort.localinteraction.domain.model.Event
import com.arathort.localinteraction.domain.repository.AdminRepository
import javax.inject.Inject

class RejectEventModerationUseCase @Inject constructor(
    private val adminRepository: AdminRepository
) {
    suspend operator fun invoke(eventId: String, reason: String): Result<Event> =
        adminRepository.rejectEventModeration(eventId, reason)
}
