package com.arathort.localinteraction.data.repository

import com.arathort.localinteraction.data.datasource.LocalInteractionDataSource
import com.arathort.localinteraction.data.mapper.toDomain
import com.arathort.localinteraction.domain.model.VolunteerApplication
import com.arathort.localinteraction.domain.repository.ApplicationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApplicationRepositoryImpl @Inject constructor(
    private val dataSource: LocalInteractionDataSource
) : ApplicationRepository {

    override fun observeApplicationsForEvent(eventId: String): Flow<List<VolunteerApplication>> =
        dataSource.observeApplications().map { list ->
            list.filter { it.eventId == eventId }.map { it.toDomain() }
        }

    override fun observeMyApplications(): Flow<List<VolunteerApplication>> =
        combine(dataSource.observeApplications(), dataSource.currentUser) { apps, user ->
            apps.filter { it.volunteerId == user.id }.map { it.toDomain() }
        }

    override suspend fun submitApplication(
        eventId: String,
        motivationLetter: String
    ): Result<VolunteerApplication> =
        dataSource.submitApplication(eventId, motivationLetter).map { it.toDomain() }

    override suspend fun withdrawApplication(applicationId: String): Result<VolunteerApplication> =
        dataSource.withdrawApplication(applicationId).map { it.toDomain() }

    override suspend fun approveApplication(applicationId: String): Result<VolunteerApplication> =
        dataSource.approveApplication(applicationId).map { it.toDomain() }

    override suspend fun rejectApplication(
        applicationId: String,
        reason: String
    ): Result<VolunteerApplication> =
        dataSource.rejectApplication(applicationId, reason).map { it.toDomain() }
}
