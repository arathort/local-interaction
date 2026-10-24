package com.arathort.localinteraction.domain.repository

import com.arathort.localinteraction.domain.model.VolunteerApplication
import kotlinx.coroutines.flow.Flow

interface ApplicationRepository {
    fun observeApplicationsForEvent(eventId: String): Flow<List<VolunteerApplication>>
    fun observeMyApplications(): Flow<List<VolunteerApplication>>
    suspend fun submitApplication(eventId: String, motivationLetter: String): Result<VolunteerApplication>
    suspend fun withdrawApplication(applicationId: String): Result<VolunteerApplication>
    suspend fun approveApplication(applicationId: String): Result<VolunteerApplication>
    suspend fun rejectApplication(applicationId: String, reason: String): Result<VolunteerApplication>
}
