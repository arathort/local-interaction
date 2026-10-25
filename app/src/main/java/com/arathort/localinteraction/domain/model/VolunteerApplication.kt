package com.arathort.localinteraction.domain.model

data class VolunteerApplication(
    val id: String,
    val eventId: String,
    val eventTitle: String,
    val eventStatus: EventStatus,
    val volunteerId: String,
    val volunteerName: String,
    val volunteerAvatarUrl: String? = null,
    val volunteerRating: Double = 0.0,
    val motivationLetter: String,
    val status: ApplicationStatus,
    val rejectionReason: String? = null,
    val createdAtMillis: Long
)
