package com.arathort.localinteraction.data.mapper

import com.arathort.localinteraction.data.dto.ReviewDto
import com.arathort.localinteraction.domain.model.Review

fun ReviewDto.toDomain(): Review = Review(
    id = id,
    eventId = eventId,
    authorId = authorId,
    authorName = authorName,
    rating = rating,
    comment = comment,
    createdAt = createdAt,
    targetUserId = targetUserId,
    eventTitle = eventTitle
)

fun Review.toDto(): ReviewDto = ReviewDto(
    id = id,
    eventId = eventId,
    authorId = authorId,
    authorName = authorName,
    rating = rating,
    comment = comment,
    createdAt = createdAt,
    targetUserId = targetUserId,
    eventTitle = eventTitle
)
