package com.arathort.localinteraction.domain.repository

import com.arathort.localinteraction.domain.model.Review
import kotlinx.coroutines.flow.Flow

interface ReviewRepository {
    fun observeReviewsForUser(userId: String): Flow<List<Review>>
    suspend fun submitReview(
        eventId: String,
        targetUserId: String,
        rating: Int,
        comment: String
    ): Result<Review>
}
