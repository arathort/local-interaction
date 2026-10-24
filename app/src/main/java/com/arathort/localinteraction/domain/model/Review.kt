package com.arathort.localinteraction.domain.model

data class Review(
    val id: String,
    val eventId: String,
    val eventTitle: String,
    val reviewerId: String,
    val reviewerName: String,
    val targetUserId: String,
    val rating: Int,
    val comment: String,
    val createdAtMillis: Long
)
