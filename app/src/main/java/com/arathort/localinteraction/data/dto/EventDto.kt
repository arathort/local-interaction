package com.arathort.localinteraction.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EventDto(
    @SerialName("id") val id: String,
    @SerialName("title") val title: String,
    @SerialName("description") val description: String,
    @SerialName("category_id") val categoryId: String,
    @SerialName("category_name") val categoryName: String,
    @SerialName("status") val status: String,
    @SerialName("date_time_millis") val dateTimeMillis: Long,
    @SerialName("address") val address: String,
    @SerialName("latitude") val latitude: Double,
    @SerialName("longitude") val longitude: Double,
    @SerialName("volunteer_quota") val volunteerQuota: Int,
    @SerialName("approved_count") val approvedCount: Int = 0,
    @SerialName("photo_urls") val photoUrls: List<String> = emptyList(),
    @SerialName("organizer_id") val organizerId: String,
    @SerialName("organizer_name") val organizerName: String,
    @SerialName("organizer_avatar_url") val organizerAvatarUrl: String? = null,
    @SerialName("organizer_rating") val organizerRating: Double = 0.0,
    @SerialName("organizer_contact_phone") val organizerContactPhone: String? = null,
    @SerialName("rejection_reason") val rejectionReason: String? = null
)
