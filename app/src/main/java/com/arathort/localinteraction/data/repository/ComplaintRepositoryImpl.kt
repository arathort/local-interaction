package com.arathort.localinteraction.data.repository

import com.arathort.localinteraction.data.datasource.remote.LocalInteractionApiService
import com.arathort.localinteraction.data.dto.ComplaintCreateDto
import com.arathort.localinteraction.data.mapper.toDomain
import com.arathort.localinteraction.domain.model.Complaint
import com.arathort.localinteraction.domain.model.ComplaintTargetType
import com.arathort.localinteraction.domain.repository.ComplaintRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ComplaintRepositoryImpl @Inject constructor(
    private val apiService: LocalInteractionApiService
) : ComplaintRepository {

    override suspend fun createComplaint(
        targetType: ComplaintTargetType,
        targetId: String,
        violationCategory: String,
        description: String
    ): Result<Complaint> = runCatching {
        val request = ComplaintCreateDto(
            targetType = targetType.apiValue,
            targetId = targetId,
            violationCategory = violationCategory.trim(),
            description = description.trim()
        )
        apiService.createComplaint(request).toDomain()
    }
}
