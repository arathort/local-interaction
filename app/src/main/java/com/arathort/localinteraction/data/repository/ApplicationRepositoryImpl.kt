package com.arathort.localinteraction.data.repository

import com.arathort.localinteraction.data.datasource.remote.LocalInteractionApiService
import com.arathort.localinteraction.data.dto.ApplicationCreateDto
import com.arathort.localinteraction.data.dto.ApplicationReviewRequestDto
import com.arathort.localinteraction.data.mapper.toDomain
import com.arathort.localinteraction.domain.model.EventApplicationsManage
import com.arathort.localinteraction.domain.model.VolunteerApplication
import com.arathort.localinteraction.domain.repository.ApplicationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApplicationRepositoryImpl @Inject constructor(
    private val apiService: LocalInteractionApiService
) : ApplicationRepository {

    override fun observeApplicationsForEvent(eventId: String): Flow<List<VolunteerApplication>> = flow {
        val applications = runCatching {
            val response = apiService.getEventApplications(eventId)
            response.toDomain().applications
        }.getOrDefault(emptyList())
        emit(applications)
    }

    override fun observeEventApplicationsManage(eventId: String): Flow<EventApplicationsManage> = flow {
        val manage = apiService.getEventApplications(eventId).toDomain()
        emit(manage)
    }

    override suspend fun getEventApplicationsManage(eventId: String): Result<EventApplicationsManage> = runCatching {
        apiService.getEventApplications(eventId).toDomain()
    }

    override fun observeMyApplications(): Flow<List<VolunteerApplication>> = flow {
        val applications = runCatching {
            apiService.getMyApplications().map { it.toDomain() }
        }.getOrDefault(emptyList())
        emit(applications)
    }

    override suspend fun getMyApplications(): Result<List<VolunteerApplication>> = runCatching {
        apiService.getMyApplications().map { it.toDomain() }
    }

    override suspend fun submitApplication(
        eventId: String,
        motivationLetter: String,
        agreedToShareContacts: Boolean
    ): Result<VolunteerApplication> = runCatching {
        val request = ApplicationCreateDto(
            motivationLetter = motivationLetter.trim().ifBlank { null },
            agreedToShareContacts = agreedToShareContacts
        )
        apiService.applyForEvent(eventId, request).toDomain()
    }

    override suspend fun withdrawApplication(applicationId: String): Result<VolunteerApplication> = runCatching {
        apiService.withdrawApplication(applicationId).toDomain()
    }

    override suspend fun approveApplication(applicationId: String): Result<VolunteerApplication> = runCatching {
        val request = ApplicationReviewRequestDto(status = "Схвалено")
        apiService.reviewVolunteerApplication(applicationId, request).toDomain()
    }

    override suspend fun rejectApplication(applicationId: String, reason: String): Result<VolunteerApplication> = runCatching {
        val request = ApplicationReviewRequestDto(status = "Відхилено", rejectionReason = reason.ifBlank { null })
        apiService.reviewVolunteerApplication(applicationId, request).toDomain()
    }
}
