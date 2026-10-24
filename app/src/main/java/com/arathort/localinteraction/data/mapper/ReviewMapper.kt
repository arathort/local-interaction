package com.arathort.localinteraction.data.mapper

import com.arathort.localinteraction.data.dto.ReviewDto
import com.arathort.localinteraction.domain.model.Review

fun ReviewDto.toDomain(): Review = Review(
    id = id,
    eventId = eventId,
    eventTitle = eventTitle,
    reviewerId = reviewerId,
    reviewerName = reviewerName,
    targetUserId = targetUserId,
    rating = rating,
    comment = comment,
    createdAtMillis = createdAtMillis
)

fun Review.toDto(): ReviewDto = ReviewDto(
    id = id,
    eventId = eventId,
    eventTitle = eventTitle,
    reviewerId = reviewerId,
    reviewerName = reviewerName,
    targetUserId = targetUserId,
    rating = rating,
    comment = comment,
    createdAtMillis = createdAtMillis
)
