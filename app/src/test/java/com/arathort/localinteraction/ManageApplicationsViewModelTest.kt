package com.arathort.localinteraction

import com.arathort.localinteraction.domain.model.ApplicationStatus
import com.arathort.localinteraction.domain.model.EventApplicationsManage
import com.arathort.localinteraction.domain.model.EventStatus
import com.arathort.localinteraction.domain.model.User
import com.arathort.localinteraction.domain.model.UserRole
import com.arathort.localinteraction.domain.model.VolunteerApplication
import com.arathort.localinteraction.domain.repository.ApplicationRepository
import com.arathort.localinteraction.domain.repository.AuthRepository
import com.arathort.localinteraction.domain.usecase.ApproveApplicationUseCase
import com.arathort.localinteraction.domain.usecase.GetEventApplicationsManageUseCase
import com.arathort.localinteraction.domain.usecase.ObserveCurrentUserUseCase
import com.arathort.localinteraction.domain.usecase.RejectApplicationUseCase
import com.arathort.localinteraction.presentation.manage_applications.ApplicationStatusFilter
import com.arathort.localinteraction.presentation.manage_applications.ManageApplicationsEffect
import com.arathort.localinteraction.presentation.manage_applications.ManageApplicationsEvent
import com.arathort.localinteraction.presentation.manage_applications.ManageApplicationsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ManageApplicationsViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private val organizerUser = User(
        id = "org-1",
        fullName = "Організатор Петро",
        role = UserRole.ORGANIZER
    )

    private val volunteerUser = User(
        id = "vol-1",
        fullName = "Волонтер Олена",
        role = UserRole.VOLUNTEER
    )

    private val currentUserFlow = MutableStateFlow(organizerUser)

    private val initialApplications = listOf(
        VolunteerApplication(
            id = "app-1",
            eventId = "event-1",
            volunteerId = "vol-1",
            volunteerName = "Олена Коваленко",
            volunteerRating = 4.9,
            motivationLetter = "Хочу допомогти",
            status = ApplicationStatus.PENDING,
            createdAt = "2026-11-08T10:00:00Z"
        ),
        VolunteerApplication(
            id = "app-2",
            eventId = "event-1",
            volunteerId = "vol-2",
            volunteerName = "Максим Іванов",
            volunteerRating = 4.5,
            volunteerPhone = "+380501234567",
            status = ApplicationStatus.APPROVED,
            createdAt = "2026-11-07T10:00:00Z"
        ),
        VolunteerApplication(
            id = "app-3",
            eventId = "event-1",
            volunteerId = "vol-3",
            volunteerName = "Тарас Шевченко",
            volunteerRating = 4.0,
            status = ApplicationStatus.REJECTED,
            rejectionReason = "Немає вільних місць",
            createdAt = "2026-11-06T10:00:00Z"
        )
    )

    private var currentApplications = initialApplications.toMutableList()

    private val fakeAuthRepository = object : AuthRepository {
        override val currentUser: Flow<User> = currentUserFlow
        override suspend fun login(email: String, password: String) = Result.success(organizerUser)
        override suspend fun register(fullName: String, email: String, password: String, role: UserRole, acceptedPrivacyPolicy: Boolean) = Result.success(organizerUser)
        override suspend fun logout() = Result.success(Unit)
        override suspend fun restoreSession() = Result.success(organizerUser)
    }

    private val fakeApplicationRepository = object : ApplicationRepository {
        override fun observeApplicationsForEvent(eventId: String): Flow<List<VolunteerApplication>> =
            flowOf(currentApplications)

        override fun observeEventApplicationsManage(eventId: String): Flow<EventApplicationsManage> =
            flowOf(
                EventApplicationsManage(
                    eventId = eventId,
                    eventTitle = "Прибирання парку",
                    eventStatus = EventStatus.OPEN,
                    progressText = "Підтверджено: 1 з 5",
                    volunteerQuota = 5,
                    approvedVolunteersCount = currentApplications.count { it.status == ApplicationStatus.APPROVED },
                    applications = currentApplications.toList()
                )
            )

        override suspend fun getEventApplicationsManage(eventId: String): Result<EventApplicationsManage> =
            Result.success(
                EventApplicationsManage(
                    eventId = eventId,
                    eventTitle = "Прибирання парку",
                    eventStatus = EventStatus.OPEN,
                    progressText = "Підтверджено: 1 з 5",
                    volunteerQuota = 5,
                    approvedVolunteersCount = currentApplications.count { it.status == ApplicationStatus.APPROVED },
                    applications = currentApplications.toList()
                )
            )

        override fun observeMyApplications(): Flow<List<VolunteerApplication>> = flowOf(emptyList())

        override suspend fun getMyApplications(): Result<List<VolunteerApplication>> = Result.success(emptyList())

        override suspend fun submitApplication(eventId: String, motivationLetter: String, agreedToShareContacts: Boolean) =
            Result.failure<VolunteerApplication>(UnsupportedOperationException())

        override suspend fun withdrawApplication(applicationId: String) =
            Result.failure<VolunteerApplication>(UnsupportedOperationException())

        override suspend fun approveApplication(applicationId: String): Result<VolunteerApplication> {
            val index = currentApplications.indexOfFirst { it.id == applicationId }
            if (index != -1) {
                val updated = currentApplications[index].copy(status = ApplicationStatus.APPROVED)
                currentApplications[index] = updated
                return Result.success(updated)
            }
            return Result.failure(NoSuchElementException())
        }

        override suspend fun rejectApplication(applicationId: String, reason: String): Result<VolunteerApplication> {
            val index = currentApplications.indexOfFirst { it.id == applicationId }
            if (index != -1) {
                val updated = currentApplications[index].copy(
                    status = ApplicationStatus.REJECTED,
                    rejectionReason = reason
                )
                currentApplications[index] = updated
                return Result.success(updated)
            }
            return Result.failure(NoSuchElementException())
        }
    }

    private fun createViewModel(quota: Int = 5): ManageApplicationsViewModel {
        return ManageApplicationsViewModel(
            eventId = "event-1",
            getEventApplicationsManageUseCase = GetEventApplicationsManageUseCase(fakeApplicationRepository),
            approveApplicationUseCase = ApproveApplicationUseCase(fakeApplicationRepository),
            rejectApplicationUseCase = RejectApplicationUseCase(fakeApplicationRepository),
            observeCurrentUserUseCase = ObserveCurrentUserUseCase(fakeAuthRepository)
        )
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        currentUserFlow.value = organizerUser
        currentApplications = initialApplications.toMutableList()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial load populates event aggregate and applications for organizer`() = runTest {
        val viewModel = createViewModel()
        val collectJob = backgroundScope.launch(testDispatcher) {
            viewModel.uiState.collect {}
        }
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.isOrganizer)
        assertEquals("Прибирання парку", state.eventTitle)
        assertEquals(5, state.volunteerQuota)
        assertEquals(1, state.approvedVolunteersCount)
        assertEquals(3, state.applications.size)
        assertEquals(1, state.pendingCount)
        assertEquals(1, state.approvedCount)
        assertEquals(1, state.rejectedCount)
        collectJob.cancel()
    }

    @Test
    fun `filter by status filters displayed applications correctly`() = runTest {
        val viewModel = createViewModel()
        val collectJob = backgroundScope.launch(testDispatcher) {
            viewModel.uiState.collect {}
        }
        advanceUntilIdle()

        viewModel.onEvent(ManageApplicationsEvent.FilterSelected(ApplicationStatusFilter.PENDING))
        assertEquals(1, viewModel.uiState.value.filteredApplications.size)
        assertEquals("app-1", viewModel.uiState.value.filteredApplications.first().id)

        viewModel.onEvent(ManageApplicationsEvent.FilterSelected(ApplicationStatusFilter.APPROVED))
        assertEquals(1, viewModel.uiState.value.filteredApplications.size)
        assertEquals("app-2", viewModel.uiState.value.filteredApplications.first().id)

        viewModel.onEvent(ManageApplicationsEvent.FilterSelected(ApplicationStatusFilter.ALL))
        assertEquals(3, viewModel.uiState.value.filteredApplications.size)
        collectJob.cancel()
    }

    @Test
    fun `approving application within quota calls approveApplication and refreshes list`() = runTest {
        val viewModel = createViewModel()
        val effects = mutableListOf<ManageApplicationsEffect>()
        val effectJob = backgroundScope.launch(testDispatcher) {
            viewModel.effect.collect { effects.add(it) }
        }
        val collectJob = backgroundScope.launch(testDispatcher) {
            viewModel.uiState.collect {}
        }
        advanceUntilIdle()

        viewModel.onEvent(ManageApplicationsEvent.ApproveClicked("app-1"))
        advanceUntilIdle()

        assertTrue(effects.contains(ManageApplicationsEffect.ShowSnackbar(R.string.manage_applications_approve_success)))
        assertEquals(2, viewModel.uiState.value.approvedVolunteersCount)
        assertEquals(0, viewModel.uiState.value.pendingCount)
        collectJob.cancel()
        effectJob.cancel()
    }

    @Test
    fun `approving application when quota reached opens quota warning dialog and confirms upon action`() = runTest {
        currentApplications = mutableListOf(
            VolunteerApplication(
                id = "app-1",
                eventId = "event-1",
                volunteerId = "vol-1",
                volunteerName = "Олена",
                status = ApplicationStatus.APPROVED,
                createdAt = "2026-11-08T10:00:00Z"
            ),
            VolunteerApplication(
                id = "app-2",
                eventId = "event-1",
                volunteerId = "vol-2",
                volunteerName = "Максим",
                status = ApplicationStatus.PENDING,
                createdAt = "2026-11-08T10:00:00Z"
            )
        )

        val tightQuotaRepo = object : ApplicationRepository by fakeApplicationRepository {
            override suspend fun getEventApplicationsManage(eventId: String): Result<EventApplicationsManage> =
                Result.success(
                    EventApplicationsManage(
                        eventId = eventId,
                        eventTitle = "Прибирання парку",
                        eventStatus = EventStatus.OPEN,
                        progressText = "Підтверджено: 1 з 1",
                        volunteerQuota = 1,
                        approvedVolunteersCount = 1,
                        applications = currentApplications.toList()
                    )
                )
        }

        val viewModel = ManageApplicationsViewModel(
            eventId = "event-1",
            getEventApplicationsManageUseCase = GetEventApplicationsManageUseCase(tightQuotaRepo),
            approveApplicationUseCase = ApproveApplicationUseCase(fakeApplicationRepository),
            rejectApplicationUseCase = RejectApplicationUseCase(fakeApplicationRepository),
            observeCurrentUserUseCase = ObserveCurrentUserUseCase(fakeAuthRepository)
        )
        val collectJob = backgroundScope.launch(testDispatcher) {
            viewModel.uiState.collect {}
        }
        advanceUntilIdle()

        viewModel.onEvent(ManageApplicationsEvent.ApproveClicked("app-2"))
        advanceUntilIdle()

        assertEquals("app-2", viewModel.uiState.value.pendingApprovalApplicationId)

        viewModel.onEvent(ManageApplicationsEvent.ConfirmOverQuotaApproval)
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.pendingApprovalApplicationId)
        collectJob.cancel()
    }

    @Test
    fun `rejecting application with empty reason shows error and does not call repository`() = runTest {
        val viewModel = createViewModel()
        val collectJob = backgroundScope.launch(testDispatcher) {
            viewModel.uiState.collect {}
        }
        advanceUntilIdle()

        viewModel.onEvent(ManageApplicationsEvent.RejectClicked("app-1"))
        viewModel.onEvent(ManageApplicationsEvent.RejectionReasonChanged("   "))
        viewModel.onEvent(ManageApplicationsEvent.ConfirmRejection)
        advanceUntilIdle()

        assertEquals(R.string.error_rejection_reason_required, viewModel.uiState.value.rejectionReasonErrorResId)
        assertEquals("app-1", viewModel.uiState.value.rejectingApplicationId)
        assertEquals(ApplicationStatus.PENDING, currentApplications.first { it.id == "app-1" }.status)
        collectJob.cancel()
    }

    @Test
    fun `rejecting application with valid reason calls rejectApplication and emits success snackbar`() = runTest {
        val viewModel = createViewModel()
        val effects = mutableListOf<ManageApplicationsEffect>()
        val effectJob = backgroundScope.launch(testDispatcher) {
            viewModel.effect.collect { effects.add(it) }
        }
        val collectJob = backgroundScope.launch(testDispatcher) {
            viewModel.uiState.collect {}
        }
        advanceUntilIdle()

        viewModel.onEvent(ManageApplicationsEvent.RejectClicked("app-1"))
        viewModel.onEvent(ManageApplicationsEvent.RejectionReasonChanged("Набір у дану групу завершено"))
        viewModel.onEvent(ManageApplicationsEvent.ConfirmRejection)
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.rejectingApplicationId)
        assertTrue(effects.contains(ManageApplicationsEffect.ShowSnackbar(R.string.manage_applications_reject_success)))
        assertEquals(ApplicationStatus.REJECTED, currentApplications.first { it.id == "app-1" }.status)
        assertEquals("Набір у дану групу завершено", currentApplications.first { it.id == "app-1" }.rejectionReason)
        collectJob.cancel()
        effectJob.cancel()
    }

    @Test
    fun `volunteer clicked emits navigation effect to volunteer profile`() = runTest {
        val viewModel = createViewModel()
        val effects = mutableListOf<ManageApplicationsEffect>()
        val effectJob = backgroundScope.launch(testDispatcher) {
            viewModel.effect.collect { effects.add(it) }
        }
        val collectJob = backgroundScope.launch(testDispatcher) {
            viewModel.uiState.collect {}
        }
        advanceUntilIdle()

        viewModel.onEvent(ManageApplicationsEvent.VolunteerClicked("vol-1"))
        advanceUntilIdle()

        assertTrue(effects.contains(ManageApplicationsEffect.NavigateToVolunteerProfile("vol-1")))
        collectJob.cancel()
        effectJob.cancel()
    }

    @Test
    fun `non-organizer user triggers role error and back navigation`() = runTest {
        currentUserFlow.value = volunteerUser
        val viewModel = createViewModel()
        val effects = mutableListOf<ManageApplicationsEffect>()
        val effectJob = backgroundScope.launch(testDispatcher) {
            viewModel.effect.collect { effects.add(it) }
        }
        val collectJob = backgroundScope.launch(testDispatcher) {
            viewModel.uiState.collect {}
        }
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isOrganizer)
        assertTrue(effects.contains(ManageApplicationsEffect.ShowSnackbar(R.string.error_only_organizer_can_manage_applications)))
        assertTrue(effects.contains(ManageApplicationsEffect.NavigateBack))
        collectJob.cancel()
        effectJob.cancel()
    }

    @Test
    fun `back clicked emits NavigateBack`() = runTest {
        val viewModel = createViewModel()
        val effects = mutableListOf<ManageApplicationsEffect>()
        val effectJob = backgroundScope.launch(testDispatcher) {
            viewModel.effect.collect { effects.add(it) }
        }
        advanceUntilIdle()

        viewModel.onEvent(ManageApplicationsEvent.BackClicked)
        advanceUntilIdle()

        assertTrue(effects.contains(ManageApplicationsEffect.NavigateBack))
        effectJob.cancel()
    }
}
