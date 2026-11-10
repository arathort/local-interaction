package com.arathort.localinteraction.data.mapper

import com.arathort.localinteraction.data.dto.CommentDto
import com.arathort.localinteraction.domain.model.Comment

fun CommentDto.toDomain(): Comment = Comment(
    id = id,
    eventId = eventId,
    authorId = authorId,
    authorName = authorName,
    authorRole = authorRole,
    authorAvatarUrl = authorAvatarUrl,
    text = text,
    replyToCommentId = replyToCommentId,
    createdAt = createdAt,
    createdAtMillis = createdAtMillis
)

fun Comment.toDto(): CommentDto = CommentDto(
    id = id,
    eventId = eventId,
    authorId = authorId,
    authorName = authorName,
    authorRole = authorRole,
    authorAvatarUrl = authorAvatarUrl,
    text = text,
    replyToCommentId = replyToCommentId,
    createdAt = createdAt,
    createdAtMillis = createdAtMillis
)
