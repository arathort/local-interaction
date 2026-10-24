package com.arathort.localinteraction.domain.repository

import com.arathort.localinteraction.domain.model.User
import com.arathort.localinteraction.domain.model.UserRole
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUser: Flow<User>
    suspend fun login(email: String, password: String): Result<User>
    suspend fun register(
        fullName: String,
        email: String,
        password: String,
        role: UserRole,
        acceptedPrivacyPolicy: Boolean
    ): Result<User>
    suspend fun logout()
    suspend fun switchDemoRole(role: UserRole): Result<User>
}
