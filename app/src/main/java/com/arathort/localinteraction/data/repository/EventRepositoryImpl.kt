package com.arathort.localinteraction.data.repository

import com.arathort.localinteraction.data.datasource.remote.LocalInteractionApiService
import com.arathort.localinteraction.data.dto.CommentCreateDto
import com.arathort.localinteraction.data.dto.EventCreateDto
import com.arathort.localinteraction.data.dto.EventStatusUpdateDto
import com.arathort.localinteraction.data.dto.EventUpdateDto
import com.arathort.localinteraction.data.dto.GeoLocationDto
import com.arathort.localinteraction.data.mapper.toDomain
import com.arathort.localinteraction.data.util.MultipartFileHelper
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
    private val apiService: LocalInteractionApiService,
    private val multipartFileHelper: MultipartFileHelper
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

    override suspend fun getOpenEvents(
        searchQuery: String,
        categoryIds: Set<String>,
        skip: Int,
        limit: Int
    ): Result<List<Event>> = runCatching {
        val categoriesParam = if (categoryIds.isEmpty()) null else categoryIds.toList()
        val searchParam = searchQuery.trim().ifBlank { null }
        val categoryMap = runCatching {
            apiService.getCategories().associate { it.id to it.name }
        }.getOrDefault(emptyMap())
        apiService.getEvents(
            search = searchParam,
            categories = categoriesParam,
            skip = skip,
            limit = limit
        ).map { dto ->
            val domain = dto.toDomain()
            if (domain.categoryName.isBlank() && domain.categoryId != null) {
                domain.copy(categoryName = categoryMap[domain.categoryId].orEmpty())
            } else {
                domain
            }
        }.filter { it.status == EventStatus.OPEN }
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

    override suspend fun getEventComments(eventId: String, skip: Int, limit: Int): Result<List<Comment>> = runCatching {
        apiService.getEventComments(eventId = eventId, skip = skip, limit = limit).map { it.toDomain() }
    }

    override suspend fun addComment(
        eventId: String,
        text: String,
        replyToCommentId: String?
    ): Result<Comment> = runCatching {
        val request = CommentCreateDto(
            text = text.trim(),
            replyToCommentId = replyToCommentId
        )
        apiService.createEventComment(eventId, request).toDomain()
    }

    override suspend fun deleteComment(commentId: String): Result<Unit> = runCatching {
        val response = apiService.deleteComment(commentId)
        if (!response.isSuccessful) {
            throw IllegalStateException("Failed to delete comment: ${response.code()}")
        }
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
        val dateString = formatIsoUtcDate(dateTimeMillis)
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
        val createdDto = apiService.createEvent(request)
        val parts = multipartFileHelper.createPartsFromUris("files", photoUrls)
        val finalDto = if (parts.isNotEmpty()) {
            runCatching {
                apiService.uploadEventPhotos(createdDto.id, parts)
            }.getOrDefault(createdDto)
        } else {
            createdDto
        }
        finalDto.toDomain()
    }

    override suspend fun updateEvent(
        eventId: String,
        title: String,
        description: String,
        categoryId: String,
        dateTimeMillis: Long,
        address: String,
        location: GeoPoint,
        volunteerQuota: Int,
        photoUrls: List<String>
    ): Result<Event> = runCatching {
        val dateString = formatIsoUtcDate(dateTimeMillis)
        val request = EventUpdateDto(
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
        val updatedDto = apiService.updateEvent(eventId, request)
        val parts = multipartFileHelper.createPartsFromUris("files", photoUrls)
        val finalDto = if (parts.isNotEmpty()) {
            runCatching {
                apiService.uploadEventPhotos(eventId, parts)
            }.getOrDefault(updatedDto)
        } else {
            updatedDto
        }
        finalDto.toDomain()
    }

    override suspend fun deleteEvent(eventId: String): Result<Unit> = runCatching {
        val response = apiService.deleteEvent(eventId)
        if (!response.isSuccessful) {
            throw IllegalStateException("Failed to delete event: ${response.code()}")
        }
    }

    override suspend fun updateEventStatus(eventId: String, newStatus: EventStatus): Result<Event> = runCatching {
        val request = EventStatusUpdateDto(status = newStatus.apiValue)
        apiService.changeEventStatus(eventId, request).toDomain()
    }

    private fun formatIsoUtcDate(dateTimeMillis: Long): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        return dateFormat.format(Date(dateTimeMillis))
    }
}
