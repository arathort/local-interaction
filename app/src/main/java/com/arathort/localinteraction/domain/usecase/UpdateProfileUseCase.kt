package com.arathort.localinteraction.domain.usecase

import com.arathort.localinteraction.domain.model.User
import com.arathort.localinteraction.domain.repository.ProfileRepository
import javax.inject.Inject

class UpdateProfileUseCase @Inject constructor(
    private val profileRepository: ProfileRepository
) {
    suspend operator fun invoke(
        fullName: String,
        phone: String,
        bio: String
    ): Result<User> {
        val trimmedName = fullName.trim()
        if (trimmedName.length < 2) {
            return Result.failure(IllegalArgumentException("Full name must contain at least 2 characters"))
        }
        return profileRepository.updateProfile(
            fullName = trimmedName,
            phone = phone.trim(),
            bio = bio.trim()
        )
    }
}
