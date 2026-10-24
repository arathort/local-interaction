package com.arathort.localinteraction.data.mapper

import com.arathort.localinteraction.data.dto.CommentDto
import com.arathort.localinteraction.domain.model.Comment

fun CommentDto.toDomain(): Comment = Comment(
    id = id,
    eventId = eventId,
    authorId = authorId,
    authorName = authorName,
    authorAvatarUrl = authorAvatarUrl,
    text = text,
    createdAtMillis = createdAtMillis
)

fun Comment.toDto(): CommentDto = CommentDto(
    id = id,
    eventId = eventId,
    authorId = authorId,
    authorName = authorName,
    authorAvatarUrl = authorAvatarUrl,
    text = text,
    createdAtMillis = createdAtMillis
)
