package com.arathort.localinteraction.domain.model

data class MyTaskHistoryItem(
    val applicationId: String,
    val eventId: String,
    val eventTitle: String,
    val eventStatus: EventStatus,
    val applicationStatus: ApplicationStatus,
    val canWithdraw: Boolean,
    val appliedAt: String
)

data class User(
    val id: String,
    val role: UserRole = UserRole.GUEST,
    val fullName: String? = null,
    val avatarUrl: String? = null,
    val description: String? = null,
    val rating: Double = 0.0,
    val reviews: List<Review> = emptyList(),
    val email: String = "",
    val phone: String? = null,
    val status: UserStatus = UserStatus.ACTIVE,
    val myTasks: List<MyTaskHistoryItem> = emptyList(),
    val blockReason: String? = null
)
