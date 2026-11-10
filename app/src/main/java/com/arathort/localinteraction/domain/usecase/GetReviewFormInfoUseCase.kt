package com.arathort.localinteraction.domain.usecase

import com.arathort.localinteraction.domain.model.ReviewTargetInfo
import com.arathort.localinteraction.domain.repository.ReviewRepository
import javax.inject.Inject

class GetReviewFormInfoUseCase @Inject constructor(
    private val reviewRepository: ReviewRepository
) {
    suspend operator fun invoke(eventId: String, targetUserId: String): Result<ReviewTargetInfo> =
        reviewRepository.getReviewFormInfo(eventId, targetUserId)
}
