package com.arathort.localinteraction.domain.usecase

import com.arathort.localinteraction.domain.model.Comment
import com.arathort.localinteraction.domain.repository.EventRepository
import javax.inject.Inject

class GetEventCommentsUseCase @Inject constructor(
    private val eventRepository: EventRepository
) {
    suspend operator fun invoke(eventId: String): Result<List<Comment>> =
        eventRepository.getEventComments(eventId)
}
