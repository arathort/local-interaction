package com.arathort.localinteraction.data.repository

import com.arathort.localinteraction.data.datasource.LocalInteractionDataSource
import com.arathort.localinteraction.data.mapper.toDomain
import com.arathort.localinteraction.domain.model.User
import com.arathort.localinteraction.domain.model.UserRole
import com.arathort.localinteraction.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val dataSource: LocalInteractionDataSource
) : AuthRepository {

    override val currentUser: Flow<User> = dataSource.currentUser.map { it.toDomain() }

    override suspend fun login(email: String, password: String): Result<User> =
        dataSource.login(email, password).map { it.toDomain() }

    override suspend fun register(
        fullName: String,
        email: String,
        password: String,
        role: UserRole,
        acceptedPrivacyPolicy: Boolean
    ): Result<User> =
        dataSource.register(fullName, email, password, role, acceptedPrivacyPolicy).map { it.toDomain() }

    override suspend fun logout() {
        dataSource.logout()
    }

    override suspend fun switchDemoRole(role: UserRole): Result<User> =
        dataSource.switchDemoRole(role).map { it.toDomain() }
}
