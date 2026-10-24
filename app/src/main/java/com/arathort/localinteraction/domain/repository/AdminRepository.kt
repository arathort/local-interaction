package com.arathort.localinteraction.domain.repository

import com.arathort.localinteraction.domain.model.Category
import com.arathort.localinteraction.domain.model.Complaint
import com.arathort.localinteraction.domain.model.Event
import com.arathort.localinteraction.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AdminRepository {
    fun observePendingModerationEvents(): Flow<List<Event>>
    suspend fun approveEventModeration(eventId: String): Result<Event>
    suspend fun rejectEventModeration(eventId: String, reason: String): Result<Event>
    suspend fun createCategory(name: String): Result<Category>
    suspend fun updateCategory(categoryId: String, name: String): Result<Category>
    suspend fun archiveCategory(categoryId: String): Result<Category>
    fun observeActiveComplaints(): Flow<List<Complaint>>
    suspend fun blockUser(userId: String, reason: String): Result<User>
}
