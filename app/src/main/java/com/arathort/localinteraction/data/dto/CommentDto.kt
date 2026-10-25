package com.arathort.localinteraction.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CommentDto(
    @SerialName("id") val id: String,
    @SerialName("event_id") val eventId: String,
    @SerialName("author_id") val authorId: String,
    @SerialName("author_name") val authorName: String,
    @SerialName("author_avatar_url") val authorAvatarUrl: String? = null,
    @SerialName("text") val text: String,
    @SerialName("created_at_millis") val createdAtMillis: Long
)
