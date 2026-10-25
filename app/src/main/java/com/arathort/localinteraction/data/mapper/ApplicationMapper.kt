package com.arathort.localinteraction.data.mapper

import com.arathort.localinteraction.data.dto.ApplicationDto
import com.arathort.localinteraction.domain.model.ApplicationStatus
import com.arathort.localinteraction.domain.model.EventStatus
import com.arathort.localinteraction.domain.model.VolunteerApplication

fun ApplicationDto.toDomain(): VolunteerApplication = VolunteerApplication(
    id = id,
    eventId = eventId,
    eventTitle = eventTitle,
    eventStatus = runCatching { EventStatus.valueOf(eventStatus) }.getOrDefault(EventStatus.OPEN),
    volunteerId = volunteerId,
    volunteerName = volunteerName,
    volunteerAvatarUrl = volunteerAvatarUrl,
    volunteerRating = volunteerRating,
    motivationLetter = motivationLetter,
    status = runCatching { ApplicationStatus.valueOf(status) }.getOrDefault(ApplicationStatus.PENDING),
    rejectionReason = rejectionReason,
    createdAtMillis = createdAtMillis
)

fun VolunteerApplication.toDto(): ApplicationDto = ApplicationDto(
    id = id,
    eventId = eventId,
    eventTitle = eventTitle,
    eventStatus = eventStatus.name,
    volunteerId = volunteerId,
    volunteerName = volunteerName,
    volunteerAvatarUrl = volunteerAvatarUrl,
    volunteerRating = volunteerRating,
    motivationLetter = motivationLetter,
    status = status.name,
    rejectionReason = rejectionReason,
    createdAtMillis = createdAtMillis
)
