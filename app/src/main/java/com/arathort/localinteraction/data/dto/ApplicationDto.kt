package com.arathort.localinteraction.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OrganizerContactsInfoDto(
    @SerialName("full_name") val fullName: String? = null,
    @SerialName("email") val email: String,
    @SerialName("phone") val phone: String? = null
)

@Serializable
data class OrganizerApplicantItemDto(
    @SerialName("application_id") val applicationId: String,
    @SerialName("volunteer_id") val volunteerId: String,
    @SerialName("volunteer_name") val volunteerName: String,
    @SerialName("volunteer_avatar_url") val volunteerAvatarUrl: String? = null,
    @SerialName("volunteer_rating") val volunteerRating: Double = 0.0,
    @SerialName("volunteer_phone") val volunteerPhone: String? = null,
    @SerialName("motivation_letter") val motivationLetter: String? = null,
    @SerialName("status") val status: String,
    @SerialName("rejection_reason") val rejectionReason: String? = null,
    @SerialName("created_at") val createdAt: String
)

@Serializable
data class EventApplicationsManageResponseDto(
    @SerialName("event_id") val eventId: String,
    @SerialName("event_title") val eventTitle: String,
    @SerialName("event_status") val eventStatus: String,
    @SerialName("progress_text") val progressText: String,
    @SerialName("volunteer_quota") val volunteerQuota: Int,
    @SerialName("approved_volunteers_count") val approvedVolunteersCount: Int,
    @SerialName("applications") val applications: List<OrganizerApplicantItemDto> = emptyList()
)

@Serializable
data class ApplicationDto(
    @SerialName("id") val id: String,
    @SerialName("event_id") val eventId: String,
    @SerialName("event_title") val eventTitle: String = "",
    @SerialName("event_date") val eventDate: String? = null,
    @SerialName("event_status") val eventStatus: String = "Відкрито",
    @SerialName("volunteer_id") val volunteerId: String,
    @SerialName("volunteer_name") val volunteerName: String = "",
    @SerialName("volunteer_avatar_url") val volunteerAvatarUrl: String? = null,
    @SerialName("volunteer_rating") val volunteerRating: Double = 0.0,
    @SerialName("volunteer_phone") val volunteerPhone: String? = null,
    @SerialName("motivation_letter") val motivationLetter: String? = null,
    @SerialName("status") val status: String,
    @SerialName("rejection_reason") val rejectionReason: String? = null,
    @SerialName("can_withdraw") val canWithdraw: Boolean = true,
    @SerialName("organizer_contacts") val organizerContacts: OrganizerContactsInfoDto? = null,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String = createdAt
)

@Serializable
data class ApplicationCreateDto(
    @SerialName("motivation_letter") val motivationLetter: String? = null,
    @SerialName("agreed_to_share_contacts") val agreedToShareContacts: Boolean
)

@Serializable
data class ApplicationReviewRequestDto(
    @SerialName("status") val status: String,
    @SerialName("rejection_reason") val rejectionReason: String? = null
)
