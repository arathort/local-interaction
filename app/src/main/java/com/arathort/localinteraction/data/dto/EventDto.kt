package com.arathort.localinteraction.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GeoLocationDto(
    @SerialName("address") val address: String,
    @SerialName("latitude") val latitude: Double? = null,
    @SerialName("longitude") val longitude: Double? = null
)

@Serializable
data class OrganizerShortInfoDto(
    @SerialName("id") val id: String,
    @SerialName("full_name") val fullName: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    @SerialName("rating") val rating: Double = 0.0
)

@Serializable
data class EventDto(
    @SerialName("id") val id: String,
    @SerialName("title") val title: String,
    @SerialName("location") val location: GeoLocationDto,
    @SerialName("event_date") val eventDate: String,
    @SerialName("category_id") val categoryId: String? = null,
    @SerialName("category_name") val categoryName: String = "",
    @SerialName("status") val status: String,
    @SerialName("moderation_status") val moderationStatus: String = "Схвалено",
    @SerialName("volunteer_quota") val volunteerQuota: Int,
    @SerialName("approved_volunteers_count") val approvedVolunteersCount: Int = 0,
    @SerialName("progress_text") val progressText: String = "Підтверджено: $approvedVolunteersCount з $volunteerQuota",
    @SerialName("photos") val photos: List<String> = emptyList(),
    @SerialName("description") val description: String = "",
    @SerialName("organizer") val organizer: OrganizerShortInfoDto = OrganizerShortInfoDto(id = ""),
    @SerialName("rejection_reason") val rejectionReason: String? = null,
    @SerialName("created_at") val createdAt: String = ""
)

@Serializable
data class EventCreateDto(
    @SerialName("title") val title: String,
    @SerialName("description") val description: String,
    @SerialName("category_id") val categoryId: String? = null,
    @SerialName("event_date") val eventDate: String,
    @SerialName("location") val location: GeoLocationDto,
    @SerialName("volunteer_quota") val volunteerQuota: Int
)

@Serializable
data class EventUpdateDto(
    @SerialName("title") val title: String? = null,
    @SerialName("description") val description: String? = null,
    @SerialName("category_id") val categoryId: String? = null,
    @SerialName("event_date") val eventDate: String? = null,
    @SerialName("location") val location: GeoLocationDto? = null,
    @SerialName("volunteer_quota") val volunteerQuota: Int? = null
)

@Serializable
data class EventStatusUpdateDto(
    @SerialName("status") val status: String
)

@Serializable
data class EventModerationRequestDto(
    @SerialName("action") val action: String,
    @SerialName("rejection_reason") val rejectionReason: String? = null
)
