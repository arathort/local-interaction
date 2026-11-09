package com.arathort.localinteraction.data.repository

import com.arathort.localinteraction.data.datasource.remote.LocalInteractionApiService
import com.arathort.localinteraction.data.dto.OrganizerReviewVolunteerCreateDto
import com.arathort.localinteraction.data.dto.VolunteerReviewOrganizerCreateDto
import com.arathort.localinteraction.data.mapper.toDomain
import com.arathort.localinteraction.domain.model.Review
import com.arathort.localinteraction.domain.model.ReviewTargetInfo
import com.arathort.localinteraction.domain.model.UserRole
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

    override suspend fun getReviewFormInfo(eventId: String, targetUserId: String): Result<ReviewTargetInfo> = runCatching {
        apiService.getReviewFormInfo(eventId, targetUserId).toDomain()
    }

    override suspend fun submitReview(
        eventId: String,
        targetUserId: String,
        rating: Int,
        comment: String,
        targetRole: UserRole
    ): Result<Review> = runCatching {
        if (targetRole == UserRole.ORGANIZER) {
            val request = VolunteerReviewOrganizerCreateDto(rating = rating, comment = comment)
            apiService.submitReviewForOrganizer(eventId, request).toDomain()
        } else {
            val request = OrganizerReviewVolunteerCreateDto(rating = rating, comment = comment, volunteerId = targetUserId)
            apiService.submitReviewForVolunteer(eventId, request).toDomain()
        }
    }

    override suspend fun getEventReviews(eventId: String): Result<List<Review>> = runCatching {
        apiService.getEventReviews(eventId).map { it.toDomain() }
    }
}
