package com.arathort.localinteraction.data.repository

import com.arathort.localinteraction.data.datasource.remote.LocalInteractionApiService
import com.arathort.localinteraction.data.mapper.toDomain
import com.arathort.localinteraction.domain.model.Review
import com.arathort.localinteraction.domain.repository.ReviewRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReviewRepositoryImpl @Inject constructor(
    private val apiService: LocalInteractionApiService
) : ReviewRepository {

    override fun observeReviewsForUser(userId: String): Flow<List<Review>> = flow {
        val reviews = runCatching {
            apiService.getUserReviews(userId).map { it.toDomain() }
        }.getOrDefault(emptyList())
        emit(reviews)
    }

    override suspend fun submitReview(
        eventId: String,
        targetUserId: String,
        rating: Int,
        comment: String
    ): Result<Review> = runCatching {
        throw UnsupportedOperationException("Submit review endpoint is pending backend implementation")
    }
}
