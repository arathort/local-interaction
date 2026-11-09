package com.arathort.localinteraction.domain.repository

import com.arathort.localinteraction.domain.model.Review
import com.arathort.localinteraction.domain.model.ReviewTargetInfo
import com.arathort.localinteraction.domain.model.UserRole
import kotlinx.coroutines.flow.Flow

interface ReviewRepository {
    fun observeReviewsForUser(userId: String): Flow<List<Review>>
    suspend fun getReviewFormInfo(eventId: String, targetUserId: String): Result<ReviewTargetInfo>
    suspend fun submitReview(
        eventId: String,
        targetUserId: String,
        rating: Int,
        comment: String,
        targetRole: UserRole = UserRole.ORGANIZER
    ): Result<Review>
    suspend fun getEventReviews(eventId: String): Result<List<Review>>
}
