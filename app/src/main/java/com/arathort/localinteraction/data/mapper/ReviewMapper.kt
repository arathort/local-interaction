package com.arathort.localinteraction.data.mapper

import com.arathort.localinteraction.data.dto.ReviewDto
import com.arathort.localinteraction.data.dto.ReviewResponseDto
import com.arathort.localinteraction.data.dto.ReviewTargetInfoDto
import com.arathort.localinteraction.domain.model.Review
import com.arathort.localinteraction.domain.model.ReviewTargetInfo
import com.arathort.localinteraction.domain.model.UserRole

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

fun ReviewResponseDto.toDomain(): Review = Review(
    id = id,
    eventId = eventId,
    authorId = reviewerId,
    authorName = reviewerName,
    rating = rating,
    comment = comment,
    createdAt = createdAt,
    targetUserId = revieweeId,
    eventTitle = eventTitle,
    newAverageRating = revieweeNewAverageRating
)

fun ReviewTargetInfoDto.toDomain(): ReviewTargetInfo = ReviewTargetInfo(
    eventId = eventId,
    eventTitle = eventTitle,
    revieweeId = revieweeId,
    revieweeName = revieweeName,
    revieweeRole = UserRole.fromApiValue(revieweeRole),
    alreadyReviewed = alreadyReviewed,
    canReview = canReview
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
