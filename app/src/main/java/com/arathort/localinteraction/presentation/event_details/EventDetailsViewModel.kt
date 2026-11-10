package com.arathort.localinteraction.presentation.event_details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arathort.localinteraction.R
import com.arathort.localinteraction.domain.model.Comment
import com.arathort.localinteraction.domain.model.UserRole
import com.arathort.localinteraction.domain.usecase.AddEventCommentUseCase
import com.arathort.localinteraction.domain.usecase.DeleteEventCommentUseCase
import com.arathort.localinteraction.domain.usecase.DeleteEventUseCase
import com.arathort.localinteraction.domain.usecase.GetEventCommentsUseCase
import com.arathort.localinteraction.domain.usecase.GetEventDetailsUseCase
import com.arathort.localinteraction.domain.usecase.GetMyApplicationsUseCase
import com.arathort.localinteraction.domain.usecase.ObserveCurrentUserUseCase
import com.arathort.localinteraction.domain.usecase.SubmitApplicationUseCase
import com.arathort.localinteraction.domain.usecase.UpdateEventStatusUseCase
import com.arathort.localinteraction.domain.usecase.WithdrawApplicationUseCase
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

@HiltViewModel(assistedFactory = EventDetailsViewModel.Factory::class)
class EventDetailsViewModel @AssistedInject constructor(
    @Assisted private val eventId: String,
    private val getEventDetailsUseCase: GetEventDetailsUseCase,
    private val observeCurrentUserUseCase: ObserveCurrentUserUseCase,
    private val getMyApplicationsUseCase: GetMyApplicationsUseCase,
    private val submitApplicationUseCase: SubmitApplicationUseCase,
    private val withdrawApplicationUseCase: WithdrawApplicationUseCase,
    private val updateEventStatusUseCase: UpdateEventStatusUseCase,
    private val getEventCommentsUseCase: GetEventCommentsUseCase,
    private val addEventCommentUseCase: AddEventCommentUseCase,
    private val deleteEventCommentUseCase: DeleteEventCommentUseCase,
    private val deleteEventUseCase: DeleteEventUseCase
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(eventId: String): EventDetailsViewModel
    }

    private val _uiState = MutableStateFlow(EventDetailsState())
    val uiState: StateFlow<EventDetailsState> = _uiState.asStateFlow()

    private val _effect = Channel<EventDetailsEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        observeCurrentUser()
        loadDetails()
        loadComments()
    }

    private fun observeCurrentUser() {
        viewModelScope.launch {
            observeCurrentUserUseCase().collect { user ->
                _uiState.update { it.copy(currentUser = user) }
                if (user.role == UserRole.VOLUNTEER) {
                    loadMyApplication()
                }
            }
        }
    }

    fun onEvent(event: EventDetailsEvent) {
        when (event) {
            EventDetailsEvent.Refresh -> {
                loadDetails()
                loadComments()
            }
            EventDetailsEvent.ApplyClicked -> handleApplyClicked()
            is EventDetailsEvent.MotivationLetterChanged -> _uiState.update { it.copy(motivationLetter = event.text) }
            is EventDetailsEvent.AgreedToShareContactsChanged -> _uiState.update {
                it.copy(agreedToShareContacts = event.agreed, applyErrorResId = null)
            }
            EventDetailsEvent.SubmitApplication -> handleSubmitApplication()
            EventDetailsEvent.DismissApplyDialog -> _uiState.update { it.copy(showApplyDialog = false) }
            EventDetailsEvent.WithdrawApplicationClicked -> handleWithdrawApplication()
            EventDetailsEvent.ManageApplicationsClicked -> {
                viewModelScope.launch {
                    _effect.send(EventDetailsEffect.NavigateToManageApplications(eventId))
                }
            }
            EventDetailsEvent.ChangeStatusClicked -> {
                _uiState.update { it.copy(showStatusDialog = true, selectedNewStatus = it.event?.status) }
            }
            is EventDetailsEvent.NewStatusSelected -> _uiState.update { it.copy(selectedNewStatus = event.status) }
            EventDetailsEvent.SubmitStatusChange -> handleSubmitStatusChange()
            EventDetailsEvent.DismissStatusDialog -> _uiState.update { it.copy(showStatusDialog = false) }
            is EventDetailsEvent.CommentTextChanged -> _uiState.update {
                it.copy(commentText = event.text, commentErrorResId = null)
            }
            is EventDetailsEvent.ReplyToCommentClicked -> handleReplyToCommentClicked(event.comment)
            EventDetailsEvent.CancelReplyClicked -> _uiState.update {
                it.copy(replyingToComment = null)
            }
            EventDetailsEvent.SubmitCommentClicked -> handleSubmitComment()
            is EventDetailsEvent.DeleteCommentClicked -> _uiState.update {
                it.copy(deletingCommentId = event.commentId)
            }
            EventDetailsEvent.ConfirmDeleteComment -> handleConfirmDeleteComment()
            EventDetailsEvent.DismissDeleteCommentDialog -> _uiState.update {
                it.copy(deletingCommentId = null)
            }
            is EventDetailsEvent.CommentAuthorClicked -> {
                if (event.authorId.isNotBlank()) {
                    viewModelScope.launch {
                        _effect.send(EventDetailsEffect.NavigateToProfile(event.authorId))
                    }
                }
            }
            EventDetailsEvent.EditEventClicked -> {
                viewModelScope.launch {
                    _effect.send(EventDetailsEffect.NavigateToEditEvent(eventId))
                }
            }
            EventDetailsEvent.DeleteEventClicked -> {
                _uiState.update { it.copy(showDeleteEventDialog = true) }
            }
            EventDetailsEvent.ConfirmDeleteEvent -> handleConfirmDeleteEvent()
            EventDetailsEvent.DismissDeleteEventDialog -> {
                _uiState.update { it.copy(showDeleteEventDialog = false) }
            }
            is EventDetailsEvent.PhotoClicked -> {
                _uiState.update { it.copy(selectedPhotoUrlForPreview = event.photoUrl) }
            }
            EventDetailsEvent.DismissPhotoPreview -> {
                _uiState.update { it.copy(selectedPhotoUrlForPreview = null) }
            }
            EventDetailsEvent.BackClicked -> {
                viewModelScope.launch {
                    _effect.send(EventDetailsEffect.NavigateBack)
                }
            }
        }
    }

    private fun handleConfirmDeleteEvent() {
        viewModelScope.launch {
            _uiState.update { it.copy(isActionLoading = true) }
            deleteEventUseCase(eventId)
                .onSuccess {
                    _uiState.update { it.copy(isActionLoading = false, showDeleteEventDialog = false) }
                    _effect.send(EventDetailsEffect.ShowSnackbar(R.string.event_delete_success))
                    _effect.send(EventDetailsEffect.NavigateBack)
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isActionLoading = false, showDeleteEventDialog = false) }
                    val message = error.message.orEmpty()
                    if (message.isNotBlank()) {
                        _effect.send(EventDetailsEffect.ShowSnackbarText(message))
                    } else {
                        _effect.send(EventDetailsEffect.ShowSnackbar(R.string.error_network))
                    }
                }
        }
    }

    private fun loadDetails() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessageResId = null) }
            getEventDetailsUseCase(eventId)
                .onSuccess { event ->
                    _uiState.update { it.copy(event = event, isLoading = false) }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, errorMessageResId = R.string.event_details_not_found) }
                    val message = error.message.orEmpty()
                    if (message.isNotBlank()) {
                        _effect.send(EventDetailsEffect.ShowSnackbarText(message))
                    }
                }
        }
    }

    private fun loadComments() {
        viewModelScope.launch {
            _uiState.update { it.copy(isCommentsLoading = true) }
            getEventCommentsUseCase(eventId)
                .onSuccess { comments ->
                    _uiState.update { it.copy(comments = comments, isCommentsLoading = false) }
                }
                .onFailure {
                    _uiState.update { it.copy(isCommentsLoading = false) }
                }
        }
    }

    private fun loadMyApplication() {
        viewModelScope.launch {
            getMyApplicationsUseCase().onSuccess { applications ->
                val myApp = applications.find { it.eventId == eventId }
                _uiState.update { it.copy(userApplication = myApp) }
            }
        }
    }

    private fun handleApplyClicked() {
        val user = _uiState.value.currentUser
        if (user.role == UserRole.GUEST) {
            viewModelScope.launch {
                _effect.send(EventDetailsEffect.NavigateToAuth)
            }
        } else {
            _uiState.update {
                it.copy(
                    showApplyDialog = true,
                    motivationLetter = "",
                    agreedToShareContacts = false,
                    applyErrorResId = null
                )
            }
        }
    }

    private fun handleReplyToCommentClicked(comment: Comment) {
        val user = _uiState.value.currentUser
        if (user.role == UserRole.GUEST) {
            viewModelScope.launch {
                _effect.send(EventDetailsEffect.NavigateToAuth)
            }
        } else {
            _uiState.update {
                it.copy(replyingToComment = comment, commentErrorResId = null)
            }
        }
    }

    private fun handleSubmitComment() {
        val state = _uiState.value
        if (state.currentUser.role == UserRole.GUEST) {
            viewModelScope.launch {
                _effect.send(EventDetailsEffect.NavigateToAuth)
            }
            return
        }

        val trimmedText = state.commentText.trim()
        if (trimmedText.isEmpty()) {
            _uiState.update { it.copy(commentErrorResId = R.string.error_comment_empty) }
            return
        }
        if (trimmedText.length > 1000) {
            _uiState.update { it.copy(commentErrorResId = R.string.error_comment_too_long) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmittingComment = true, commentErrorResId = null) }
            addEventCommentUseCase(
                eventId = eventId,
                text = trimmedText,
                replyToCommentId = state.replyingToComment?.id
            )
                .onSuccess { newComment ->
                    _uiState.update {
                        it.copy(
                            isSubmittingComment = false,
                            commentText = "",
                            replyingToComment = null,
                            comments = it.comments + newComment
                        )
                    }
                    _effect.send(EventDetailsEffect.ShowSnackbar(R.string.event_details_comment_added))
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isSubmittingComment = false) }
                    val message = error.message.orEmpty()
                    if (message.isNotBlank()) {
                        _effect.send(EventDetailsEffect.ShowSnackbarText(message))
                    } else {
                        _effect.send(EventDetailsEffect.ShowSnackbar(R.string.error_network))
                    }
                }
        }
    }

    private fun handleConfirmDeleteComment() {
        val commentId = _uiState.value.deletingCommentId ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isActionLoading = true) }
            deleteEventCommentUseCase(commentId)
                .onSuccess {
                    _uiState.update { state ->
                        state.copy(
                            isActionLoading = false,
                            deletingCommentId = null,
                            comments = state.comments.filterNot { it.id == commentId }
                        )
                    }
                    _effect.send(EventDetailsEffect.ShowSnackbar(R.string.event_details_comment_deleted))
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(isActionLoading = false, deletingCommentId = null)
                    }
                    val message = error.message.orEmpty()
                    if (message.isNotBlank()) {
                        _effect.send(EventDetailsEffect.ShowSnackbarText(message))
                    } else {
                        _effect.send(EventDetailsEffect.ShowSnackbar(R.string.error_network))
                    }
                }
        }
    }

    private fun handleSubmitApplication() {
        val state = _uiState.value
        if (!state.agreedToShareContacts) {
            _uiState.update { it.copy(applyErrorResId = R.string.error_privacy_consent_required) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isActionLoading = true) }
            submitApplicationUseCase(
                eventId = eventId,
                motivationLetter = state.motivationLetter,
                agreedToShareContacts = state.agreedToShareContacts
            )
                .onSuccess { application ->
                    _uiState.update {
                        it.copy(
                            isActionLoading = false,
                            showApplyDialog = false,
                            userApplication = application
                        )
                    }
                    _effect.send(EventDetailsEffect.ShowSnackbar(R.string.event_details_application_submitted))
                    loadDetails()
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isActionLoading = false) }
                    val message = error.message.orEmpty().ifBlank { "Помилка при поданні заявки" }
                    _effect.send(EventDetailsEffect.ShowSnackbarText(message))
                }
        }
    }

    private fun handleWithdrawApplication() {
        val application = _uiState.value.userApplication ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isActionLoading = true) }
            withdrawApplicationUseCase(application.id)
                .onSuccess { updatedApp ->
                    _uiState.update {
                        it.copy(
                            isActionLoading = false,
                            userApplication = updatedApp
                        )
                    }
                    _effect.send(EventDetailsEffect.ShowSnackbar(R.string.event_details_application_withdrawn))
                    loadDetails()
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isActionLoading = false) }
                    val message = error.message.orEmpty().ifBlank { "Помилка при відкликанні заявки" }
                    _effect.send(EventDetailsEffect.ShowSnackbarText(message))
                }
        }
    }

    private fun handleSubmitStatusChange() {
        val newStatus = _uiState.value.selectedNewStatus ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isActionLoading = true) }
            updateEventStatusUseCase(eventId, newStatus)
                .onSuccess { updatedEvent ->
                    _uiState.update {
                        it.copy(
                            isActionLoading = false,
                            showStatusDialog = false,
                            event = updatedEvent
                        )
                    }
                    _effect.send(EventDetailsEffect.ShowSnackbar(R.string.event_details_status_updated))
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isActionLoading = false) }
                    val message = error.message.orEmpty().ifBlank { "Помилка при зміні статусу" }
                    _effect.send(EventDetailsEffect.ShowSnackbarText(message))
                }
        }
    }
}
