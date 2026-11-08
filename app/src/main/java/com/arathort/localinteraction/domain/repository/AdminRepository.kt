package com.arathort.localinteraction.domain.repository

import com.arathort.localinteraction.domain.model.Category
import com.arathort.localinteraction.domain.model.Complaint
import com.arathort.localinteraction.domain.model.Event
import com.arathort.localinteraction.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AdminRepository {
    fun observePendingModerationEvents(): Flow<List<Event>>
    suspend fun getPendingModerationEvents(): Result<List<Event>>
    suspend fun approveEventModeration(eventId: String): Result<Event>
    suspend fun rejectEventModeration(eventId: String, reason: String): Result<Event>

    fun observeAdminCategories(): Flow<List<Category>>
    suspend fun getAdminCategories(): Result<List<Category>>
    suspend fun createCategory(name: String): Result<Category>
    suspend fun updateCategory(categoryId: String, name: String): Result<Category>
    suspend fun archiveCategory(categoryId: String): Result<Category>
    suspend fun deleteCategory(categoryId: String): Result<String>

    fun observeActiveComplaints(): Flow<List<Complaint>>
    suspend fun getAdminComplaints(statusFilter: String? = null): Result<List<Complaint>>
    suspend fun rejectComplaint(complaintId: String, adminNote: String? = null): Result<Complaint>
    suspend fun deleteComplaintContent(complaintId: String): Result<Complaint>

    suspend fun getAdminUsers(role: String? = null, status: String? = null): Result<List<User>>
    suspend fun blockUser(userId: String, reason: String, complaintId: String? = null): Result<User>
    suspend fun unblockUser(userId: String): Result<User>
}
