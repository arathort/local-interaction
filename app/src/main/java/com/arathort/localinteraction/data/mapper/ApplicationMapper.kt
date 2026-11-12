package com.arathort.localinteraction.data.mapper

import com.arathort.localinteraction.data.dto.ApplicationDto
import com.arathort.localinteraction.data.dto.EventApplicationsManageResponseDto
import com.arathort.localinteraction.data.dto.OrganizerApplicantItemDto
import com.arathort.localinteraction.data.dto.OrganizerContactsInfoDto
import com.arathort.localinteraction.domain.model.ApplicationStatus
import com.arathort.localinteraction.domain.model.EventApplicationsManage
import com.arathort.localinteraction.domain.model.EventStatus
import com.arathort.localinteraction.domain.model.OrganizerContactsInfo
import com.arathort.localinteraction.domain.model.VolunteerApplication

fun OrganizerContactsInfoDto.toDomain(): OrganizerContactsInfo = OrganizerContactsInfo(
    fullName = fullName,
    email = email,
    phone = phone
)

fun OrganizerContactsInfo.toDto(): OrganizerContactsInfoDto = OrganizerContactsInfoDto(
    fullName = fullName,
    email = email,
    phone = phone
)

fun OrganizerApplicantItemDto.toDomain(
    eventId: String = "",
    eventTitle: String = "",
    eventStatus: EventStatus = EventStatus.OPEN
): VolunteerApplication = VolunteerApplication(
    id = applicationId,
    eventId = eventId,
    eventTitle = eventTitle,
    eventStatus = eventStatus,
    volunteerId = volunteerId,
    volunteerName = volunteerName,
    volunteerAvatarUrl = volunteerAvatarUrl,
    volunteerRating = volunteerRating,
    volunteerPhone = volunteerPhone,
    motivationLetter = motivationLetter,
    status = ApplicationStatus.fromApiValue(status),
    rejectionReason = rejectionReason,
    canWithdraw = eventStatus != EventStatus.COMPLETED && eventStatus != EventStatus.CANCELLED,
    createdAt = createdAt,
    updatedAt = createdAt
)

fun VolunteerApplication.toOrganizerApplicantDto(): OrganizerApplicantItemDto = OrganizerApplicantItemDto(
    applicationId = id,
    volunteerId = volunteerId,
    volunteerName = volunteerName,
    volunteerAvatarUrl = volunteerAvatarUrl,
    volunteerRating = volunteerRating,
    volunteerPhone = volunteerPhone,
    motivationLetter = motivationLetter,
    status = status.apiValue,
    rejectionReason = rejectionReason,
    createdAt = createdAt
)

fun EventApplicationsManageResponseDto.toDomain(): EventApplicationsManage {
    val parsedEventStatus = EventStatus.fromApiValue(eventStatus)
    return EventApplicationsManage(
        eventId = eventId,
        eventTitle = eventTitle,
        eventStatus = parsedEventStatus,
        progressText = progressText,
        volunteerQuota = volunteerQuota,
        approvedVolunteersCount = approvedVolunteersCount,
        applications = applications.map {
            it.toDomain(
                eventId = eventId,
                eventTitle = eventTitle,
                eventStatus = parsedEventStatus
            )
        }
    )
}

fun ApplicationDto.toDomain(): VolunteerApplication = VolunteerApplication(
    id = id,
    eventId = eventId,
    eventTitle = eventTitle,
    eventDate = eventDate,
    eventStatus = EventStatus.fromApiValue(eventStatus),
    volunteerId = volunteerId,
    volunteerName = volunteerName,
    volunteerAvatarUrl = volunteerAvatarUrl,
    volunteerRating = volunteerRating,
    volunteerPhone = volunteerPhone,
    motivationLetter = motivationLetter,
    status = ApplicationStatus.fromApiValue(status),
    rejectionReason = rejectionReason,
    canWithdraw = canWithdraw,
    organizerContacts = organizerContacts?.toDomain(),
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun VolunteerApplication.toDto(): ApplicationDto = ApplicationDto(
    id = id,
    eventId = eventId,
    eventTitle = eventTitle,
    eventDate = eventDate,
    eventStatus = eventStatus.apiValue,
    volunteerId = volunteerId,
    volunteerName = volunteerName,
    volunteerAvatarUrl = volunteerAvatarUrl,
    volunteerRating = volunteerRating,
    volunteerPhone = volunteerPhone,
    motivationLetter = motivationLetter,
    status = status.apiValue,
    rejectionReason = rejectionReason,
    canWithdraw = canWithdraw,
    organizerContacts = organizerContacts?.toDto(),
    createdAt = createdAt,
    updatedAt = updatedAt
)
