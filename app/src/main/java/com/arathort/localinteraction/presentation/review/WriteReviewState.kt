package com.arathort.localinteraction.presentation.review

import com.arathort.localinteraction.domain.model.UserRole

data class WriteReviewState(
    val eventId: String = "",
    val eventTitle: String = "",
    val targetUserId: String = "",
    val targetUserName: String = "",
    val targetRole: UserRole = UserRole.ORGANIZER,
    val rating: Int = 0,
    val comment: String = "",
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val alreadyReviewed: Boolean = false,
    val canReview: Boolean = true,
    val ratingErrorRes: Int? = null,
    val commentErrorRes: Int? = null,
    val generalError: String? = null
) {
    val isSubmitEnabled: Boolean
        get() = rating in 1..5 &&
            comment.trim().length >= 2 &&
            !isSubmitting &&
            !isLoading &&
            canReview &&
            !alreadyReviewed

    val commentLength: Int
        get() = comment.length
}
