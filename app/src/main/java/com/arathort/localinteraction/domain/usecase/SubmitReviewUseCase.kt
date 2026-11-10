package com.arathort.localinteraction.domain.usecase

import com.arathort.localinteraction.domain.model.Review
import com.arathort.localinteraction.domain.model.UserRole
import com.arathort.localinteraction.domain.repository.ReviewRepository
import javax.inject.Inject

class SubmitReviewUseCase @Inject constructor(
    private val reviewRepository: ReviewRepository
) {
    suspend operator fun invoke(
        eventId: String,
        targetUserId: String,
        rating: Int,
        comment: String,
        targetRole: UserRole = UserRole.ORGANIZER
    ): Result<Review> =
        reviewRepository.submitReview(eventId, targetUserId, rating, comment, targetRole)
}
