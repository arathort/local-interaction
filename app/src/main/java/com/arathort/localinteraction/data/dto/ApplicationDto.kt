package com.arathort.localinteraction.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApplicationDto(
    @SerialName("id") val id: String,
    @SerialName("event_id") val eventId: String,
    @SerialName("event_title") val eventTitle: String,
    @SerialName("event_status") val eventStatus: String,
    @SerialName("volunteer_id") val volunteerId: String,
    @SerialName("volunteer_name") val volunteerName: String,
    @SerialName("volunteer_avatar_url") val volunteerAvatarUrl: String? = null,
    @SerialName("volunteer_rating") val volunteerRating: Double = 0.0,
    @SerialName("motivation_letter") val motivationLetter: String,
    @SerialName("status") val status: String,
    @SerialName("rejection_reason") val rejectionReason: String? = null,
    @SerialName("created_at_millis") val createdAtMillis: Long
)
