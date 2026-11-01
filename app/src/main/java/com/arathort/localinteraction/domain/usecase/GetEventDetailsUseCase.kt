package com.arathort.localinteraction.domain.usecase

import com.arathort.localinteraction.domain.model.Event
import com.arathort.localinteraction.domain.repository.EventRepository
import javax.inject.Inject

class GetEventDetailsUseCase @Inject constructor(
    private val eventRepository: EventRepository
) {
    suspend operator fun invoke(eventId: String): Result<Event> =
        eventRepository.getEventById(eventId)
}
