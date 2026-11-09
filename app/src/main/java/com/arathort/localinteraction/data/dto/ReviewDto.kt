package com.arathort.localinteraction.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ReviewDto(
    @SerialName("id") val id: String,
    @SerialName("event_id") val eventId: String,
    @SerialName("author_id") val authorId: String,
    @SerialName("author_name") val authorName: String,
    @SerialName("rating") val rating: Int,
    @SerialName("comment") val comment: String? = null,
    @SerialName("created_at") val createdAt: String,
    @SerialName("target_user_id") val targetUserId: String = "",
    @SerialName("event_title") val eventTitle: String = ""
)

@Serializable
data class ReviewTargetInfoDto(
    @SerialName("event_id") val eventId: String,
    @SerialName("event_title") val eventTitle: String,
    @SerialName("reviewee_id") val revieweeId: String,
    @SerialName("reviewee_name") val revieweeName: String,
    @SerialName("reviewee_role") val revieweeRole: String,
    @SerialName("already_reviewed") val alreadyReviewed: Boolean,
    @SerialName("can_review") val canReview: Boolean
)

@Serializable
data class VolunteerReviewOrganizerCreateDto(
    @SerialName("rating") val rating: Int,
    @SerialName("comment") val comment: String
)

@Serializable
data class OrganizerReviewVolunteerCreateDto(
    @SerialName("rating") val rating: Int,
    @SerialName("comment") val comment: String,
    @SerialName("volunteer_id") val volunteerId: String
)

@Serializable
data class ReviewResponseDto(
    @SerialName("id") val id: String,
    @SerialName("event_id") val eventId: String,
    @SerialName("event_title") val eventTitle: String,
    @SerialName("reviewer_id") val reviewerId: String,
    @SerialName("reviewer_name") val reviewerName: String,
    @SerialName("reviewee_id") val revieweeId: String,
    @SerialName("reviewee_name") val revieweeName: String,
    @SerialName("rating") val rating: Int,
    @SerialName("comment") val comment: String? = null,
    @SerialName("reviewee_new_average_rating") val revieweeNewAverageRating: Double = 0.0,
    @SerialName("created_at") val createdAt: String
)
