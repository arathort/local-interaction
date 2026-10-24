package com.arathort.localinteraction.domain.model

data class User(
    val id: String,
    val fullName: String,
    val email: String,
    val phone: String = "",
    val bio: String = "",
    val avatarUrl: String? = null,
    val role: UserRole = UserRole.GUEST,
    val averageRating: Double = 0.0,
    val reviewsCount: Int = 0,
    val isBlocked: Boolean = false,
    val blockReason: String? = null
)
