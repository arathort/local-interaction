package com.arathort.localinteraction.domain.usecase

import com.arathort.localinteraction.domain.model.User
import com.arathort.localinteraction.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveCurrentUserUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    operator fun invoke(): Flow<User> = authRepository.currentUser
}
