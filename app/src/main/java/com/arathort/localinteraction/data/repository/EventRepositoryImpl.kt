package com.arathort.localinteraction.data.repository

import com.arathort.localinteraction.data.datasource.LocalInteractionDataSource
import com.arathort.localinteraction.data.mapper.toDomain
import com.arathort.localinteraction.domain.model.Category
import com.arathort.localinteraction.domain.model.Comment
import com.arathort.localinteraction.domain.model.Event
import com.arathort.localinteraction.domain.model.EventStatus
import com.arathort.localinteraction.domain.model.GeoPoint
import com.arathort.localinteraction.domain.repository.EventRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EventRepositoryImpl @Inject constructor(
    private val dataSource: LocalInteractionDataSource
) : EventRepository {

    override fun observeOpenEvents(
        searchQuery: String,
        categoryIds: Set<String>
    ): Flow<List<Event>> = dataSource.observeEvents().map { dtoList ->
        val normalizedQuery = searchQuery.trim()
        dtoList
            .map { it.toDomain() }
            .filter { event ->
                event.status == EventStatus.OPEN &&
                    (categoryIds.isEmpty() || (event.categoryId != null && event.categoryId in categoryIds)) &&
                    (normalizedQuery.isEmpty() ||
                        event.title.contains(normalizedQuery, ignoreCase = true) ||
                        event.description.contains(normalizedQuery, ignoreCase = true))
            }
    }

    override fun observeCategories(): Flow<List<Category>> =
        dataSource.observeCategories().map { list ->
            list.filter { !it.isArchived }.map { it.toDomain() }
        }

    override suspend fun getEventById(eventId: String): Result<Event> =
        dataSource.getEventById(eventId).map { it.toDomain() }

    override fun observeCommentsForEvent(eventId: String): Flow<List<Comment>> =
        dataSource.observeComments().map { list ->
            list.filter { it.eventId == eventId }.map { it.toDomain() }
        }

    override suspend fun addComment(eventId: String, text: String): Result<Comment> =
        dataSource.addComment(eventId, text).map { it.toDomain() }

    override suspend fun createEvent(
        title: String,
        description: String,
        categoryId: String,
        dateTimeMillis: Long,
        address: String,
        location: GeoPoint,
        volunteerQuota: Int,
        photoUrls: List<String>
    ): Result<Event> = dataSource.createEvent(
        title = title,
        description = description,
        categoryId = categoryId,
        dateTimeMillis = dateTimeMillis,
        address = address,
        location = location,
        volunteerQuota = volunteerQuota,
        photoUrls = photoUrls
    ).map { it.toDomain() }

    override suspend fun updateEventStatus(eventId: String, newStatus: EventStatus): Result<Event> =
        dataSource.updateEventStatus(eventId, newStatus).map { it.toDomain() }
}
