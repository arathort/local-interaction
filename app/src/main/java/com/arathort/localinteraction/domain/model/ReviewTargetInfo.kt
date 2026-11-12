package com.arathort.localinteraction.domain.model

data class ReviewTargetInfo(
    val eventId: String,
    val eventTitle: String,
    val revieweeId: String,
    val revieweeName: String,
    val revieweeRole: UserRole,
    val alreadyReviewed: Boolean,
    val canReview: Boolean
)
