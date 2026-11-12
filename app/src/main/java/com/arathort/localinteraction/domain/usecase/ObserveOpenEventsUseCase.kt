package com.arathort.localinteraction.domain.usecase

import com.arathort.localinteraction.domain.model.Event
import com.arathort.localinteraction.domain.repository.EventRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveOpenEventsUseCase @Inject constructor(
    private val eventRepository: EventRepository
) {
    operator fun invoke(
        searchQuery: String = "",
        categoryIds: Set<String> = emptySet()
    ): Flow<List<Event>> = eventRepository.observeOpenEvents(
        searchQuery = searchQuery,
        categoryIds = categoryIds
    )
}
