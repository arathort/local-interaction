package com.arathort.localinteraction.domain.usecase

import com.arathort.localinteraction.domain.model.Event
import com.arathort.localinteraction.domain.repository.EventRepository
import javax.inject.Inject

class GetOpenEventsUseCase @Inject constructor(
    private val eventRepository: EventRepository
) {
    suspend operator fun invoke(
        searchQuery: String = "",
        categoryIds: Set<String> = emptySet(),
        skip: Int = 0,
        limit: Int = 20
    ): Result<List<Event>> = eventRepository.getOpenEvents(
        searchQuery = searchQuery,
        categoryIds = categoryIds,
        skip = skip,
        limit = limit
    )
}
