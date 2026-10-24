package com.arathort.localinteraction.data.mapper

import com.arathort.localinteraction.data.dto.ComplaintDto
import com.arathort.localinteraction.domain.model.Complaint

fun ComplaintDto.toDomain(): Complaint = Complaint(
    id = id,
    reporterId = reporterId,
    reporterName = reporterName,
    violatorId = violatorId,
    violatorName = violatorName,
    reason = reason,
    createdAtMillis = createdAtMillis,
    isResolved = isResolved
)

fun Complaint.toDto(): ComplaintDto = ComplaintDto(
    id = id,
    reporterId = reporterId,
    reporterName = reporterName,
    violatorId = violatorId,
    violatorName = violatorName,
    reason = reason,
    createdAtMillis = createdAtMillis,
    isResolved = isResolved
)
