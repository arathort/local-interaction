package com.arathort.localinteraction.domain.usecase

import com.arathort.localinteraction.domain.model.VolunteerApplication
import com.arathort.localinteraction.domain.repository.ApplicationRepository
import javax.inject.Inject

class SubmitApplicationUseCase @Inject constructor(
    private val applicationRepository: ApplicationRepository
) {
    suspend operator fun invoke(
        eventId: String,
        motivationLetter: String,
        agreedToShareContacts: Boolean
    ): Result<VolunteerApplication> = applicationRepository.submitApplication(
        eventId = eventId,
        motivationLetter = motivationLetter,
        agreedToShareContacts = agreedToShareContacts
    )
}
