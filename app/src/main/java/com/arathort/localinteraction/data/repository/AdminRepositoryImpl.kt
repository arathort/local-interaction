package com.arathort.localinteraction.data.repository

import com.arathort.localinteraction.data.datasource.remote.LocalInteractionApiService
import com.arathort.localinteraction.data.dto.CategoryCreateUpdateRequestDto
import com.arathort.localinteraction.data.dto.ComplaintRejectRequestDto
import com.arathort.localinteraction.data.dto.EventModerationRequestDto
import com.arathort.localinteraction.data.dto.UserBlockRequestDto
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

    override suspend fun getPendingModerationEvents(): Result<List<Event>> = runCatching {
        apiService.getPendingModerationEvents().map { it.toDomain() }
    }

    override suspend fun approveEventModeration(eventId: String): Result<Event> = runCatching {
        val request = EventModerationRequestDto(action = "Approved")
        apiService.moderateEvent(eventId, request).toDomain()
    }

    override suspend fun rejectEventModeration(eventId: String, reason: String): Result<Event> = runCatching {
        val request = EventModerationRequestDto(action = "Rejected", rejectionReason = reason)
        apiService.moderateEvent(eventId, request).toDomain()
    }

    override fun observeAdminCategories(): Flow<List<Category>> = flow {
        val categories = runCatching {
            apiService.getCategoriesAdmin().map { it.toDomain() }
        }.getOrDefault(emptyList())
        emit(categories)
    }

    override suspend fun getAdminCategories(): Result<List<Category>> = runCatching {
        apiService.getCategoriesAdmin().map { it.toDomain() }
    }

    override suspend fun createCategory(name: String): Result<Category> = runCatching {
        val request = CategoryCreateUpdateRequestDto(name = name)
        apiService.createCategory(request).toDomain()
    }

    override suspend fun updateCategory(categoryId: String, name: String): Result<Category> = runCatching {
        val request = CategoryCreateUpdateRequestDto(name = name)
        apiService.updateCategory(categoryId, request).toDomain()
    }

    override suspend fun archiveCategory(categoryId: String): Result<Category> = runCatching {
        apiService.deleteCategory(categoryId)
        Category(id = categoryId, name = "", isArchived = true)
    }

    override suspend fun deleteCategory(categoryId: String): Result<String> = runCatching {
        apiService.deleteCategory(categoryId).action
    }

    override fun observeActiveComplaints(): Flow<List<Complaint>> = flow {
        val complaints = runCatching {
            apiService.getAdminComplaints().filter { !it.status.equals("Resolved", ignoreCase = true) }.map { it.toDomain() }
        }.getOrDefault(emptyList())
        emit(complaints)
    }

    override suspend fun getAdminComplaints(statusFilter: String?): Result<List<Complaint>> = runCatching {
        apiService.getAdminComplaints(statusFilter).map { it.toDomain() }
    }

    override suspend fun rejectComplaint(complaintId: String, adminNote: String?): Result<Complaint> = runCatching {
        val request = ComplaintRejectRequestDto(adminNote = adminNote)
        apiService.rejectComplaint(complaintId, request).toDomain()
    }

    override suspend fun deleteComplaintContent(complaintId: String): Result<Complaint> = runCatching {
        apiService.deleteReportedContent(complaintId).toDomain()
    }

    override suspend fun getAdminUsers(role: String?, status: String?): Result<List<User>> = runCatching {
        apiService.getAdminUsers(role = role, accountStatus = status).map { it.toDomain() }
    }

    override suspend fun blockUser(userId: String, reason: String, complaintId: String?): Result<User> = runCatching {
        val request = UserBlockRequestDto(reason = reason, complaintId = complaintId)
        apiService.blockUser(userId, request).toDomain()
    }

    override suspend fun unblockUser(userId: String): Result<User> = runCatching {
        apiService.unblockUser(userId).toDomain()
    }
}
