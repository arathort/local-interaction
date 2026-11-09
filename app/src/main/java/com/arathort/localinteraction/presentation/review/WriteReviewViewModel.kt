package com.arathort.localinteraction.presentation.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arathort.localinteraction.R
import com.arathort.localinteraction.domain.usecase.GetReviewFormInfoUseCase
import com.arathort.localinteraction.domain.usecase.SubmitReviewUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = WriteReviewViewModel.Factory::class)
class WriteReviewViewModel @AssistedInject constructor(
    @Assisted("eventId") private val eventId: String,
    @Assisted("eventTitle") private val initialEventTitle: String,
    @Assisted("targetUserId") private val targetUserId: String,
    @Assisted("targetUserName") private val initialTargetUserName: String,
    private val getReviewFormInfoUseCase: GetReviewFormInfoUseCase,
    private val submitReviewUseCase: SubmitReviewUseCase
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted("eventId") eventId: String,
            @Assisted("eventTitle") eventTitle: String,
            @Assisted("targetUserId") targetUserId: String,
            @Assisted("targetUserName") targetUserName: String
        ): WriteReviewViewModel
    }

    private val _uiState = MutableStateFlow(
        WriteReviewState(
            eventId = eventId,
            eventTitle = initialEventTitle,
            targetUserId = targetUserId,
            targetUserName = initialTargetUserName
        )
    )
    val uiState: StateFlow<WriteReviewState> = _uiState.asStateFlow()

    private val effectChannel = Channel<WriteReviewEffect>(Channel.BUFFERED)
    val effect = effectChannel.receiveAsFlow()

    init {
        loadReviewFormInfo()
    }

    fun onEvent(event: WriteReviewEvent) {
        when (event) {
            is WriteReviewEvent.RatingChanged -> handleRatingChanged(event.rating)
            is WriteReviewEvent.CommentChanged -> handleCommentChanged(event.comment)
            WriteReviewEvent.SubmitClicked -> handleSubmit()
            WriteReviewEvent.CancelClicked -> viewModelScope.launch { effectChannel.send(WriteReviewEffect.NavigateBack) }
            WriteReviewEvent.RetryClicked -> loadReviewFormInfo()
        }
    }

    private fun loadReviewFormInfo() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, generalError = null) }
            getReviewFormInfoUseCase(eventId, targetUserId)
                .onSuccess { info ->
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            eventTitle = info.eventTitle.ifBlank { state.eventTitle },
                            targetUserName = info.revieweeName.ifBlank { state.targetUserName },
                            targetRole = info.revieweeRole,
                            alreadyReviewed = info.alreadyReviewed,
                            canReview = info.canReview
                        )
                    }
                }
                .onFailure {
                    _uiState.update { state ->
                        state.copy(isLoading = false)
                    }
                }
        }
    }

    private fun handleRatingChanged(newRating: Int) {
        val clamped = newRating.coerceIn(1, 5)
        _uiState.update {
            it.copy(
                rating = clamped,
                ratingErrorRes = null
            )
        }
    }

    private fun handleCommentChanged(newComment: String) {
        val trimmedComment = if (newComment.length > 1000) newComment.take(1000) else newComment
        _uiState.update {
            it.copy(
                comment = trimmedComment,
                commentErrorRes = null
            )
        }
    }

    private fun handleSubmit() {
        val state = _uiState.value

        if (state.alreadyReviewed) {
            viewModelScope.launch {
                effectChannel.send(WriteReviewEffect.ShowSnackbar(R.string.error_review_already_submitted))
            }
            return
        }

        if (!state.canReview) {
            viewModelScope.launch {
                effectChannel.send(WriteReviewEffect.ShowSnackbar(R.string.error_review_event_status_invalid))
            }
            return
        }

        if (state.rating !in 1..5) {
            _uiState.update { it.copy(ratingErrorRes = R.string.error_review_rating_invalid) }
            return
        }

        val trimmedComment = state.comment.trim()
        if (trimmedComment.length < 2) {
            _uiState.update { it.copy(commentErrorRes = R.string.error_review_comment_required) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, generalError = null) }
            submitReviewUseCase(
                eventId = state.eventId,
                targetUserId = state.targetUserId,
                rating = state.rating,
                comment = trimmedComment,
                targetRole = state.targetRole
            ).onSuccess { review ->
                _uiState.update { it.copy(isSubmitting = false, alreadyReviewed = true) }
                val effect = if (review.newAverageRating != null) {
                    WriteReviewEffect.ShowSnackbar(
                        R.string.review_success_with_new_rating,
                        listOf(review.newAverageRating)
                    )
                } else {
                    WriteReviewEffect.ShowSnackbar(R.string.review_success)
                }
                effectChannel.send(effect)
                effectChannel.send(WriteReviewEffect.NavigateBack)
            }.onFailure { error ->
                _uiState.update { it.copy(isSubmitting = false, generalError = error.message) }
                effectChannel.send(WriteReviewEffect.ShowMessage(error.message.orEmpty()))
            }
        }
    }
}
