package com.arathort.localinteraction.domain.repository

import com.arathort.localinteraction.domain.model.Category
import com.arathort.localinteraction.domain.model.Comment
import com.arathort.localinteraction.domain.model.Event
import com.arathort.localinteraction.domain.model.EventStatus
import com.arathort.localinteraction.domain.model.GeoPoint
import kotlinx.coroutines.flow.Flow

interface EventRepository {
    fun observeOpenEvents(
        searchQuery: String = "",
        categoryIds: Set<String> = emptySet()
    ): Flow<List<Event>>
    fun observeCategories(): Flow<List<Category>>
    suspend fun getEventById(eventId: String): Result<Event>
    fun observeCommentsForEvent(eventId: String): Flow<List<Comment>>
    suspend fun addComment(eventId: String, text: String): Result<Comment>
    suspend fun createEvent(
        title: String,
        description: String,
        categoryId: String,
        dateTimeMillis: Long,
        address: String,
        location: GeoPoint,
        volunteerQuota: Int,
        photoUrls: List<String> = emptyList()
    ): Result<Event>
    suspend fun updateEventStatus(eventId: String, newStatus: EventStatus): Result<Event>
}
