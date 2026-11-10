package com.arathort.localinteraction.domain.usecase

import com.arathort.localinteraction.domain.model.User
import com.arathort.localinteraction.domain.repository.ProfileRepository
import javax.inject.Inject

class UploadAvatarUseCase @Inject constructor(
    private val profileRepository: ProfileRepository
) {
    suspend operator fun invoke(uriString: String): Result<User> {
        if (uriString.isBlank()) {
            return Result.failure(IllegalArgumentException("Image URI cannot be blank"))
        }
        return profileRepository.uploadAvatar(uriString)
    }
}
