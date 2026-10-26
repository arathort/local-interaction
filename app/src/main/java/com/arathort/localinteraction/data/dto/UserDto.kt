package com.arathort.localinteraction.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MyTaskHistoryItemDto(
    @SerialName("application_id") val applicationId: String,
    @SerialName("event_id") val eventId: String,
    @SerialName("event_title") val eventTitle: String,
    @SerialName("event_status") val eventStatus: String,
    @SerialName("application_status") val applicationStatus: String,
    @SerialName("can_withdraw") val canWithdraw: Boolean,
    @SerialName("applied_at") val appliedAt: String
)

@Serializable
data class UserDto(
    @SerialName("id") val id: String,
    @SerialName("role") val role: String = "Гість",
    @SerialName("full_name") val fullName: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    @SerialName("description") val description: String? = null,
    @SerialName("rating") val rating: Double = 0.0,
    @SerialName("reviews") val reviews: List<ReviewDto> = emptyList(),
    @SerialName("email") val email: String = "",
    @SerialName("phone") val phone: String? = null,
    @SerialName("status") val status: String = "Активний",
    @SerialName("my_tasks") val myTasks: List<MyTaskHistoryItemDto> = emptyList(),
    @SerialName("block_reason") val blockReason: String? = null
)

@Serializable
data class UserCreateDto(
    @SerialName("email") val email: String,
    @SerialName("password") val password: String,
    @SerialName("password_confirm") val passwordConfirm: String,
    @SerialName("role") val role: String,
    @SerialName("agreed_to_rules") val agreedToRules: Boolean
)

@Serializable
data class ProfileUpdateDto(
    @SerialName("full_name") val fullName: String,
    @SerialName("phone") val phone: String? = null,
    @SerialName("description") val description: String? = null
)
