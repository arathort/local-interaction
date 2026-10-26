package com.arathort.localinteraction.domain.model

data class Review(
    val id: String,
    val eventId: String,
    val authorId: String,
    val authorName: String,
    val rating: Int,
    val comment: String? = null,
    val createdAt: String,
    val targetUserId: String = "",
    val eventTitle: String = ""
)
