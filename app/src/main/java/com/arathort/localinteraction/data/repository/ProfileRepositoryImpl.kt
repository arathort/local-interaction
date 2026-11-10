package com.arathort.localinteraction.data.repository

import com.arathort.localinteraction.data.datasource.remote.LocalInteractionApiService
import com.arathort.localinteraction.data.dto.ProfileUpdateDto
import com.arathort.localinteraction.data.mapper.toDomain
import com.arathort.localinteraction.data.util.MultipartFileHelper
import com.arathort.localinteraction.domain.model.Event
import com.arathort.localinteraction.domain.model.User
import com.arathort.localinteraction.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepositoryImpl @Inject constructor(
    private val apiService: LocalInteractionApiService,
    private val multipartFileHelper: MultipartFileHelper
) : ProfileRepository {

    override suspend fun getMyProfile(): Result<User> = runCatching {
        apiService.getMyProfile().toDomain()
    }

    override suspend fun getUserProfile(userId: String): Result<User> = runCatching {
        apiService.getUserPublicProfile(userId).toDomain()
    }

    override suspend fun updateProfile(
        fullName: String,
        phone: String,
        bio: String
    ): Result<User> = runCatching {
        val request = ProfileUpdateDto(
            fullName = fullName,
            phone = phone.ifBlank { null },
            description = bio.ifBlank { null }
        )
        apiService.updateMyProfile(request).toDomain()
    }

    override suspend fun uploadAvatar(uriString: String): Result<User> = runCatching {
        val part = multipartFileHelper.createPartFromUri("file", uriString)
            ?: throw IllegalArgumentException("Invalid image URI")
        apiService.uploadAvatar(part)
        apiService.getMyProfile().toDomain()
    }

    override suspend fun deleteAvatar(): Result<User> = runCatching {
        apiService.deleteAvatar()
        apiService.getMyProfile().toDomain()
    }

    override fun observeOrganizerEvents(organizerId: String): Flow<List<Event>> = flow {
        val events = runCatching {
            apiService.getMyEvents().map { it.toDomain() }
        }.getOrDefault(emptyList())
        emit(events)
    }
}
