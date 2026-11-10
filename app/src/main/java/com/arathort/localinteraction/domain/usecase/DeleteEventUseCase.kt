package com.arathort.localinteraction.domain.usecase

import com.arathort.localinteraction.domain.repository.EventRepository
import javax.inject.Inject

class DeleteEventUseCase @Inject constructor(
    private val eventRepository: EventRepository
) {
    suspend operator fun invoke(eventId: String): Result<Unit> {
        if (eventId.isBlank()) {
            return Result.failure(IllegalArgumentException("Event ID cannot be blank"))
        }
        return eventRepository.deleteEvent(eventId)
    }
}
