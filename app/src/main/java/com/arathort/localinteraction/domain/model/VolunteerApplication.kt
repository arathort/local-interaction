package com.arathort.localinteraction.domain.model

data class OrganizerContactsInfo(
    val fullName: String? = null,
    val email: String,
    val phone: String? = null
)

data class EventApplicationsManage(
    val eventId: String,
    val eventTitle: String,
    val eventStatus: EventStatus,
    val progressText: String,
    val volunteerQuota: Int,
    val approvedVolunteersCount: Int,
    val applications: List<VolunteerApplication>
)

data class VolunteerApplication(
    val id: String,
    val eventId: String,
    val eventTitle: String = "",
    val eventDate: String? = null,
    val eventStatus: EventStatus = EventStatus.OPEN,
    val volunteerId: String,
    val volunteerName: String = "",
    val volunteerAvatarUrl: String? = null,
    val volunteerRating: Double = 0.0,
    val volunteerPhone: String? = null,
    val motivationLetter: String? = null,
    val status: ApplicationStatus = ApplicationStatus.PENDING,
    val rejectionReason: String? = null,
    val canWithdraw: Boolean = true,
    val organizerContacts: OrganizerContactsInfo? = null,
    val createdAt: String,
    val updatedAt: String = createdAt
)
