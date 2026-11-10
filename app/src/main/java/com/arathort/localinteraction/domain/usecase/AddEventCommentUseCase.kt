package com.arathort.localinteraction.domain.usecase

import com.arathort.localinteraction.domain.model.Comment
import com.arathort.localinteraction.domain.repository.EventRepository
import javax.inject.Inject

class AddEventCommentUseCase @Inject constructor(
    private val eventRepository: EventRepository
) {
    suspend operator fun invoke(
        eventId: String,
        text: String,
        replyToCommentId: String? = null
    ): Result<Comment> {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) {
            return Result.failure(IllegalArgumentException("Comment text cannot be empty"))
        }
        if (trimmed.length > 1000) {
            return Result.failure(IllegalArgumentException("Comment text cannot exceed 1000 characters"))
        }
        return eventRepository.addComment(
            eventId = eventId,
            text = trimmed,
            replyToCommentId = replyToCommentId
        )
    }
}
