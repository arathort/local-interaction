package com.arathort.localinteraction.domain.model

data class Comment(
    val id: String,
    val eventId: String,
    val authorId: String,
    val authorName: String,
    val authorAvatarUrl: String? = null,
    val text: String,
    val createdAtMillis: Long
)
