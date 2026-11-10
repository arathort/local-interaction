package com.arathort.localinteraction.domain.usecase

import com.arathort.localinteraction.domain.repository.EventRepository
import javax.inject.Inject

class DeleteEventCommentUseCase @Inject constructor(
    private val eventRepository: EventRepository
) {
    suspend operator fun invoke(commentId: String): Result<Unit> =
        eventRepository.deleteComment(commentId)
}
