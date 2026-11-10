package com.arathort.localinteraction.presentation.review

sealed interface WriteReviewEffect {
    data class ShowSnackbar(val stringResId: Int, val formatArgs: List<Any> = emptyList()) : WriteReviewEffect
    data class ShowMessage(val message: String) : WriteReviewEffect
    data object NavigateBack : WriteReviewEffect
}
