package com.arathort.localinteraction.domain.usecase

import com.arathort.localinteraction.domain.model.EventApplicationsManage
import com.arathort.localinteraction.domain.repository.ApplicationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetEventApplicationsManageUseCase @Inject constructor(
    private val applicationRepository: ApplicationRepository
) {
    operator fun invoke(eventId: String): Flow<EventApplicationsManage> =
        applicationRepository.observeEventApplicationsManage(eventId)

    suspend fun fetch(eventId: String): Result<EventApplicationsManage> =
        applicationRepository.getEventApplicationsManage(eventId)
}
