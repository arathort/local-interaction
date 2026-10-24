package com.arathort.localinteraction.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    @SerialName("id") val id: String,
    @SerialName("full_name") val fullName: String,
    @SerialName("email") val email: String,
    @SerialName("phone") val phone: String = "",
    @SerialName("bio") val bio: String = "",
    @SerialName("avatar_url") val avatarUrl: String? = null,
    @SerialName("role") val role: String = "GUEST",
    @SerialName("average_rating") val averageRating: Double = 0.0,
    @SerialName("reviews_count") val reviewsCount: Int = 0,
    @SerialName("is_blocked") val isBlocked: Boolean = false,
    @SerialName("block_reason") val blockReason: String? = null
)
