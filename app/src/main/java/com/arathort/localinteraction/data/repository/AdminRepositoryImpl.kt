package com.arathort.localinteraction.data.repository

import com.arathort.localinteraction.data.datasource.remote.LocalInteractionApiService
import com.arathort.localinteraction.data.dto.EventModerationRequestDto
import com.arathort.localinteraction.data.mapper.toDomain
import com.arathort.localinteraction.domain.model.Category
import com.arathort.localinteraction.domain.model.Complaint
import com.arathort.localinteraction.domain.model.Event
import com.arathort.localinteraction.domain.model.User
import com.arathort.localinteraction.domain.repository.AdminRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdminRepositoryImpl @Inject constructor(
    private val apiService: LocalInteractionApiService
) : AdminRepository {

    override fun observePendingModerationEvents(): Flow<List<Event>> = flow {
        val events = runCatching {
            apiService.getPendingModerationEvents().map { it.toDomain() }
        }.getOrDefault(emptyList())
        emit(events)
    }

    override suspend fun approveEventModeration(eventId: String): Result<Event> = runCatching {
        val request = EventModerationRequestDto(action = "Схвалено")
        apiService.moderateEvent(eventId, request).toDomain()
    }

    override suspend fun rejectEventModeration(eventId: String, reason: String): Result<Event> = runCatching {
        val request = EventModerationRequestDto(action = "Відхилено", rejectionReason = reason)
        apiService.moderateEvent(eventId, request).toDomain()
    }

    override suspend fun createCategory(name: String): Result<Category> = runCatching {
        throw UnsupportedOperationException("Category creation endpoint is pending backend implementation")
    }

    override suspend fun updateCategory(categoryId: String, name: String): Result<Category> = runCatching {
        throw UnsupportedOperationException("Category update endpoint is pending backend implementation")
    }

    override suspend fun archiveCategory(categoryId: String): Result<Category> = runCatching {
        throw UnsupportedOperationException("Category archive endpoint is pending backend implementation")
    }

    override fun observeActiveComplaints(): Flow<List<Complaint>> = flow {
        val complaints = runCatching {
            apiService.getComplaints().filter { !it.isResolved }.map { it.toDomain() }
        }.getOrDefault(emptyList())
        emit(complaints)
    }

    override suspend fun blockUser(userId: String, reason: String): Result<User> = runCatching {
        throw UnsupportedOperationException("Block user endpoint is pending backend implementation")
    }
}
