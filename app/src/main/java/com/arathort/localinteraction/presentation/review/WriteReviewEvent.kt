package com.arathort.localinteraction.presentation.review

sealed interface WriteReviewEvent {
    data class RatingChanged(val rating: Int) : WriteReviewEvent
    data class CommentChanged(val comment: String) : WriteReviewEvent
    data object SubmitClicked : WriteReviewEvent
    data object CancelClicked : WriteReviewEvent
    data object RetryClicked : WriteReviewEvent
}
