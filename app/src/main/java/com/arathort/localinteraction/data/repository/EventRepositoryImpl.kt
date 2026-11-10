package com.arathort.localinteraction.data.repository

import com.arathort.localinteraction.data.datasource.remote.LocalInteractionApiService
import com.arathort.localinteraction.data.dto.CommentCreateDto
import com.arathort.localinteraction.data.dto.EventCreateDto
import com.arathort.localinteraction.data.dto.EventStatusUpdateDto
import com.arathort.localinteraction.data.dto.GeoLocationDto
import com.arathort.localinteraction.data.mapper.toDomain
import com.arathort.localinteraction.domain.model.Category
import com.arathort.localinteraction.domain.model.Comment
import com.arathort.localinteraction.domain.model.Event
import com.arathort.localinteraction.domain.model.EventStatus
import com.arathort.localinteraction.domain.model.GeoPoint
import com.arathort.localinteraction.domain.repository.EventRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EventRepositoryImpl @Inject constructor(
    private val apiService: LocalInteractionApiService
) : EventRepository {

    override fun observeOpenEvents(
        searchQuery: String,
        categoryIds: Set<String>
    ): Flow<List<Event>> = flow {
        val categoriesParam = if (categoryIds.isEmpty()) null else categoryIds.toList()
        val searchParam = searchQuery.trim().ifBlank { null }
        val events = runCatching {
            val categoryMap = runCatching {
                apiService.getCategories().associate { it.id to it.name }
            }.getOrDefault(emptyMap())
            apiService.getEvents(search = searchParam, categories = categoriesParam)
                .map { dto ->
                    val domain = dto.toDomain()
                    if (domain.categoryName.isBlank() && domain.categoryId != null) {
                        domain.copy(categoryName = categoryMap[domain.categoryId].orEmpty())
                    } else {
                        domain
                    }
                }
                .filter { it.status == EventStatus.OPEN }
        }.getOrDefault(emptyList())
        emit(events)
    }

    override fun observeCategories(): Flow<List<Category>> = flow {
        val categories = runCatching {
            apiService.getCategories()
                .filter { !it.isArchived }
                .map { it.toDomain() }
        }.getOrDefault(emptyList())
        emit(categories)
    }

    override suspend fun getEventById(eventId: String): Result<Event> = runCatching {
        val domain = apiService.getEventById(eventId).toDomain()
        if (domain.categoryName.isBlank() && domain.categoryId != null) {
            val categoryName = runCatching {
                apiService.getCategories().find { it.id == domain.categoryId }?.name.orEmpty()
            }.getOrDefault("")
            domain.copy(categoryName = categoryName)
        } else {
            domain
        }
    }

    override fun observeCommentsForEvent(eventId: String): Flow<List<Comment>> = flow {
        val comments = runCatching {
            apiService.getEventComments(eventId).map { it.toDomain() }
        }.getOrDefault(emptyList())
        emit(comments)
    }

    override suspend fun addComment(eventId: String, text: String): Result<Comment> = runCatching {
        val request = CommentCreateDto(text = text.trim())
        apiService.createEventComment(eventId, request).toDomain()
    }

    override suspend fun createEvent(
        title: String,
        description: String,
        categoryId: String,
        dateTimeMillis: Long,
        address: String,
        location: GeoPoint,
        volunteerQuota: Int,
        photoUrls: List<String>
    ): Result<Event> = runCatching {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val dateString = dateFormat.format(Date(dateTimeMillis))
        val request = EventCreateDto(
            title = title,
            description = description,
            categoryId = categoryId.ifBlank { null },
            eventDate = dateString,
            location = GeoLocationDto(
                address = address,
                latitude = location.latitude,
                longitude = location.longitude
            ),
            volunteerQuota = volunteerQuota
        )
        apiService.createEvent(request).toDomain()
    }

    override suspend fun updateEventStatus(eventId: String, newStatus: EventStatus): Result<Event> = runCatching {
        val request = EventStatusUpdateDto(status = newStatus.apiValue)
        apiService.changeEventStatus(eventId, request).toDomain()
    }
}
