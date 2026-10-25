package com.arathort.localinteraction.data.repository

import com.arathort.localinteraction.data.datasource.LocalInteractionDataSource
import com.arathort.localinteraction.data.mapper.toDomain
import com.arathort.localinteraction.domain.model.Category
import com.arathort.localinteraction.domain.model.Complaint
import com.arathort.localinteraction.domain.model.Event
import com.arathort.localinteraction.domain.model.EventStatus
import com.arathort.localinteraction.domain.model.User
import com.arathort.localinteraction.domain.repository.AdminRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdminRepositoryImpl @Inject constructor(
    private val dataSource: LocalInteractionDataSource
) : AdminRepository {

    override fun observePendingModerationEvents(): Flow<List<Event>> =
        dataSource.observeEvents().map { list ->
            list.map { it.toDomain() }.filter { it.status == EventStatus.PENDING_MODERATION }
        }

    override suspend fun approveEventModeration(eventId: String): Result<Event> =
        dataSource.approveEventModeration(eventId).map { it.toDomain() }

    override suspend fun rejectEventModeration(eventId: String, reason: String): Result<Event> =
        dataSource.rejectEventModeration(eventId, reason).map { it.toDomain() }

    override suspend fun createCategory(name: String): Result<Category> =
        dataSource.createCategory(name).map { it.toDomain() }

    override suspend fun updateCategory(categoryId: String, name: String): Result<Category> =
        dataSource.updateCategory(categoryId, name).map { it.toDomain() }

    override suspend fun archiveCategory(categoryId: String): Result<Category> =
        dataSource.archiveCategory(categoryId).map { it.toDomain() }

    override fun observeActiveComplaints(): Flow<List<Complaint>> =
        dataSource.observeComplaints().map { list ->
            list.filter { !it.isResolved }.map { it.toDomain() }
        }

    override suspend fun blockUser(userId: String, reason: String): Result<User> =
        dataSource.blockUser(userId, reason).map { it.toDomain() }
}
