package com.arathort.localinteraction.data.mapper

import com.arathort.localinteraction.data.dto.ComplaintDto
import com.arathort.localinteraction.data.dto.ComplaintResponseDto
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

fun ComplaintResponseDto.toDomain(): Complaint = Complaint(
    id = id,
    reporterId = reporterId,
    reporterName = reporterName,
    violatorId = accusedUserId ?: "",
    violatorName = accusedUserName ?: "",
    reason = description,
    createdAtMillis = 0L,
    isResolved = status.equals("Resolved", ignoreCase = true),
    targetType = targetType,
    targetId = targetId,
    targetSummary = targetSummary,
    violationCategory = violationCategory,
    status = status,
    adminNote = adminNote,
    createdAt = createdAt
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
