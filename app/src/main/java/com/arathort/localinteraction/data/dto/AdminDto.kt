package com.arathort.localinteraction.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CategoryAdminResponseDto(
    @SerialName("id") val id: String,
    @SerialName("name") val name: String,
    @SerialName("active_events_count") val activeEventsCount: Int = 0,
    @SerialName("total_events_count") val totalEventsCount: Int = 0,
    @SerialName("is_archived") val isArchived: Boolean = false,
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("updated_at") val updatedAt: String? = null
)

@Serializable
data class CategoryCreateUpdateRequestDto(
    @SerialName("name") val name: String
)

@Serializable
data class CategoryDeleteResponseDto(
    @SerialName("id") val id: String,
    @SerialName("name") val name: String,
    @SerialName("action") val action: String,
    @SerialName("message") val message: String
)

@Serializable
data class AdminUserRegistryItemDto(
    @SerialName("id") val id: String,
    @SerialName("email") val email: String,
    @SerialName("full_name") val fullName: String,
    @SerialName("role") val role: String,
    @SerialName("status") val status: String,
    @SerialName("rating") val rating: Double = 0.0,
    @SerialName("block_reason") val blockReason: String? = null,
    @SerialName("blocked_at") val blockedAt: String? = null
)

@Serializable
data class UserBlockRequestDto(
    @SerialName("reason") val reason: String,
    @SerialName("complaint_id") val complaintId: String? = null
)

@Serializable
data class ComplaintResponseDto(
    @SerialName("id") val id: String,
    @SerialName("reporter_id") val reporterId: String,
    @SerialName("reporter_name") val reporterName: String,
    @SerialName("target_type") val targetType: String,
    @SerialName("target_id") val targetId: String,
    @SerialName("target_summary") val targetSummary: String,
    @SerialName("accused_user_id") val accusedUserId: String? = null,
    @SerialName("accused_user_name") val accusedUserName: String? = null,
    @SerialName("violation_category") val violationCategory: String,
    @SerialName("description") val description: String,
    @SerialName("status") val status: String,
    @SerialName("admin_note") val adminNote: String? = null,
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("resolved_at") val resolvedAt: String? = null
)

@Serializable
data class ComplaintRejectRequestDto(
    @SerialName("admin_note") val adminNote: String? = null
)
