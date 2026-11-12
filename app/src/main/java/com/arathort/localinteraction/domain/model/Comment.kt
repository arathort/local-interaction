package com.arathort.localinteraction.domain.model

data class Comment(
    val id: String,
    val eventId: String,
    val authorId: String,
    val authorName: String,
    val authorRole: String = "",
    val authorAvatarUrl: String? = null,
    val text: String,
    val replyToCommentId: String? = null,
    val createdAt: String = "",
    val createdAtMillis: Long = 0L
)
