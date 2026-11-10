package com.arathort.localinteraction.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CommentDto(
    @SerialName("id") val id: String,
    @SerialName("event_id") val eventId: String,
    @SerialName("author_id") val authorId: String,
    @SerialName("author_name") val authorName: String,
    @SerialName("author_role") val authorRole: String = "",
    @SerialName("author_avatar_url") val authorAvatarUrl: String? = null,
    @SerialName("text") val text: String,
    @SerialName("reply_to_comment_id") val replyToCommentId: String? = null,
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("created_at_millis") val createdAtMillis: Long = 0L
)

@Serializable
data class CommentCreateDto(
    @SerialName("text") val text: String,
    @SerialName("reply_to_comment_id") val replyToCommentId: String? = null
)
