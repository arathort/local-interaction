package com.arathort.localinteraction.presentation.event_details

import com.arathort.localinteraction.domain.model.Comment
import com.arathort.localinteraction.domain.model.EventStatus

sealed interface EventDetailsEvent {
    data object Refresh : EventDetailsEvent
    data object ApplyClicked : EventDetailsEvent
    data class MotivationLetterChanged(val text: String) : EventDetailsEvent
    data class AgreedToShareContactsChanged(val agreed: Boolean) : EventDetailsEvent
    data object SubmitApplication : EventDetailsEvent
    data object DismissApplyDialog : EventDetailsEvent
    data object WithdrawApplicationClicked : EventDetailsEvent
    data object ManageApplicationsClicked : EventDetailsEvent
    data object ChangeStatusClicked : EventDetailsEvent
    data class NewStatusSelected(val status: EventStatus) : EventDetailsEvent
    data object SubmitStatusChange : EventDetailsEvent
    data object DismissStatusDialog : EventDetailsEvent
    data class CommentTextChanged(val text: String) : EventDetailsEvent
    data class ReplyToCommentClicked(val comment: Comment) : EventDetailsEvent
    data object CancelReplyClicked : EventDetailsEvent
    data object SubmitCommentClicked : EventDetailsEvent
    data class DeleteCommentClicked(val commentId: String) : EventDetailsEvent
    data object ConfirmDeleteComment : EventDetailsEvent
    data object DismissDeleteCommentDialog : EventDetailsEvent
    data class CommentAuthorClicked(val authorId: String) : EventDetailsEvent
    data object BackClicked : EventDetailsEvent
}
