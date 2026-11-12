package com.arathort.localinteraction.domain.usecase

import com.arathort.localinteraction.domain.model.Event
import com.arathort.localinteraction.domain.repository.AdminRepository
import javax.inject.Inject

class GetPendingModerationEventsUseCase @Inject constructor(
    private val adminRepository: AdminRepository
) {
    suspend operator fun invoke(): Result<List<Event>> =
        adminRepository.getPendingModerationEvents()
}
