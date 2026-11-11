package com.arathort.localinteraction.domain.repository

import com.arathort.localinteraction.domain.model.Complaint
import com.arathort.localinteraction.domain.model.ComplaintTargetType

interface ComplaintRepository {
    suspend fun createComplaint(
        targetType: ComplaintTargetType,
        targetId: String,
        violationCategory: String,
        description: String
    ): Result<Complaint>
}
