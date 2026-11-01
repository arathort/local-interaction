package com.arathort.localinteraction.domain.usecase

import com.arathort.localinteraction.domain.model.Event
import com.arathort.localinteraction.domain.model.EventStatus
import com.arathort.localinteraction.domain.repository.EventRepository
import javax.inject.Inject

class UpdateEventStatusUseCase @Inject constructor(
    private val eventRepository: EventRepository
) {
    suspend operator fun invoke(eventId: String, newStatus: EventStatus): Result<Event> =
        eventRepository.updateEventStatus(eventId, newStatus)
}
