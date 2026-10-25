package com.arathort.localinteraction.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ReviewDto(
    @SerialName("id") val id: String,
    @SerialName("event_id") val eventId: String,
    @SerialName("event_title") val eventTitle: String,
    @SerialName("reviewer_id") val reviewerId: String,
    @SerialName("reviewer_name") val reviewerName: String,
    @SerialName("target_user_id") val targetUserId: String,
    @SerialName("rating") val rating: Int,
    @SerialName("comment") val comment: String,
    @SerialName("created_at_millis") val createdAtMillis: Long
)
