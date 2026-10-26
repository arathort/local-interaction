package com.arathort.localinteraction.data.repository

import com.arathort.localinteraction.data.datasource.LocalInteractionDataSource
import com.arathort.localinteraction.data.mapper.toDomain
import com.arathort.localinteraction.domain.model.Event
import com.arathort.localinteraction.domain.model.User
import com.arathort.localinteraction.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepositoryImpl @Inject constructor(
    private val dataSource: LocalInteractionDataSource
) : ProfileRepository {

    override suspend fun getUserProfile(userId: String): Result<User> =
        dataSource.getUserProfile(userId).map { it.toDomain() }

    override suspend fun updateProfile(
        fullName: String,
        phone: String,
        bio: String
    ): Result<User> =
        dataSource.updateProfile(fullName, phone, bio).map { it.toDomain() }

    override fun observeOrganizerEvents(organizerId: String): Flow<List<Event>> =
        dataSource.observeEvents().map { list ->
            list.filter { it.organizer.id == organizerId }.map { it.toDomain() }
        }
}
