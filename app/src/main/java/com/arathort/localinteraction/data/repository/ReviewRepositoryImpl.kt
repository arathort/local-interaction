package com.arathort.localinteraction.data.repository

import com.arathort.localinteraction.data.datasource.LocalInteractionDataSource
import com.arathort.localinteraction.data.mapper.toDomain
import com.arathort.localinteraction.domain.model.Review
import com.arathort.localinteraction.domain.repository.ReviewRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReviewRepositoryImpl @Inject constructor(
    private val dataSource: LocalInteractionDataSource
) : ReviewRepository {

    override fun observeReviewsForUser(userId: String): Flow<List<Review>> =
        dataSource.observeReviews().map { list ->
            list.filter { it.targetUserId == userId }.map { it.toDomain() }
        }

    override suspend fun submitReview(
        eventId: String,
        targetUserId: String,
        rating: Int,
        comment: String
    ): Result<Review> =
        dataSource.submitReview(eventId, targetUserId, rating, comment).map { it.toDomain() }
}
