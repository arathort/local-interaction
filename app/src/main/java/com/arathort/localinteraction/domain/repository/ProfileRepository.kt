package com.arathort.localinteraction.domain.repository

import com.arathort.localinteraction.domain.model.Event
import com.arathort.localinteraction.domain.model.User
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
    suspend fun getUserProfile(userId: String): Result<User>
    suspend fun updateProfile(fullName: String, phone: String, bio: String): Result<User>
    fun observeOrganizerEvents(organizerId: String): Flow<List<Event>>
}
