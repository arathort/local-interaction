package com.arathort.localinteraction.domain.usecase

import com.arathort.localinteraction.domain.model.Category
import com.arathort.localinteraction.domain.repository.EventRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveCategoriesUseCase @Inject constructor(
    private val eventRepository: EventRepository
) {
    operator fun invoke(): Flow<List<Category>> = eventRepository.observeCategories()
}
