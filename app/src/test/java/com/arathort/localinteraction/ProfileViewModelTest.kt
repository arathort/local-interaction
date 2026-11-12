package com.arathort.localinteraction

import com.arathort.localinteraction.domain.model.ApplicationStatus
import com.arathort.localinteraction.domain.model.Complaint
import com.arathort.localinteraction.domain.model.ComplaintTargetType
import com.arathort.localinteraction.domain.model.Event
import com.arathort.localinteraction.domain.model.EventApplicationsManage
import com.arathort.localinteraction.domain.model.EventStatus
import com.arathort.localinteraction.domain.model.GeoPoint
import com.arathort.localinteraction.domain.model.MyTaskHistoryItem
import com.arathort.localinteraction.domain.model.OrganizerShortInfo
import com.arathort.localinteraction.domain.model.Review
import com.arathort.localinteraction.domain.model.User
import com.arathort.localinteraction.domain.model.UserRole
import com.arathort.localinteraction.domain.model.UserStatus
import com.arathort.localinteraction.domain.model.VolunteerApplication
import com.arathort.localinteraction.domain.repository.ApplicationRepository
import com.arathort.localinteraction.domain.repository.AuthRepository
import com.arathort.localinteraction.domain.repository.ComplaintRepository
import com.arathort.localinteraction.domain.repository.ProfileRepository
import com.arathort.localinteraction.domain.usecase.DeleteAvatarUseCase
import com.arathort.localinteraction.domain.usecase.GetMyProfileUseCase
import com.arathort.localinteraction.domain.usecase.GetUserProfileUseCase
import com.arathort.localinteraction.domain.usecase.LogoutUseCase
import com.arathort.localinteraction.domain.usecase.ObserveCurrentUserUseCase
import com.arathort.localinteraction.domain.usecase.SubmitComplaintUseCase
import com.arathort.localinteraction.domain.usecase.UpdateProfileUseCase
import com.arathort.localinteraction.domain.usecase.UploadAvatarUseCase
import com.arathort.localinteraction.domain.usecase.WithdrawApplicationUseCase
import com.arathort.localinteraction.presentation.profile.ProfileEffect
import com.arathort.localinteraction.presentation.profile.ProfileEvent
import com.arathort.localinteraction.presentation.profile.ProfileTab
import com.arathort.localinteraction.presentation.profile.ProfileViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
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
class ProfileViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private val testVolunteerUser = User(
        id = "user-vol-1",
        role = UserRole.VOLUNTEER,
        fullName = "Оксана Петренко",
        email = "oxana@example.com",
        phone = "+380671112233",
        description = "Волонтер з досвідом",
        rating = 4.8,
        status = UserStatus.ACTIVE,
        reviews = listOf(
            Review(
                id = "rev-1",
                eventId = "ev-1",
                authorId = "org-1",
                authorName = "Фонд Добра",
                rating = 5,
                comment = "Відмінна співпраця!",
                createdAt = "2026-11-01T10:00:00Z"
            )
        ),
        myTasks = listOf(
            MyTaskHistoryItem(
                applicationId = "app-1",
                eventId = "ev-1",
                eventTitle = "Прибирання парку",
                eventStatus = EventStatus.OPEN,
                applicationStatus = ApplicationStatus.APPROVED,
                canWithdraw = true,
                appliedAt = "2026-11-02T12:00:00Z"
            )
        )
    )

    private val testExternalUser = User(
        id = "user-external",
        role = UserRole.ORGANIZER,
        fullName = "Іван Організатор",
        email = "ivan@example.com",
        rating = 4.5,
        status = UserStatus.ACTIVE
    )

    private val currentUserFlow = MutableStateFlow(testVolunteerUser)

    private val fakeAuthRepository = object : AuthRepository {
        override val currentUser: Flow<User> = currentUserFlow
        override suspend fun login(email: String, password: String) = Result.success(testVolunteerUser)
        override suspend fun register(fullName: String, email: String, password: String, role: UserRole, acceptedPrivacyPolicy: Boolean) = Result.success(testVolunteerUser)
        override suspend fun logout(): Result<Unit> {
            currentUserFlow.value = User(id = "user-guest", role = UserRole.GUEST)
            return Result.success(Unit)
        }
        override suspend fun restoreSession() = Result.success(testVolunteerUser)
    }

    private val fakeProfileRepository = object : ProfileRepository {
        override suspend fun getMyProfile(): Result<User> = Result.success(testVolunteerUser)
        override suspend fun getUserProfile(userId: String): Result<User> = Result.success(testExternalUser)
        override suspend fun updateProfile(fullName: String, phone: String, bio: String): Result<User> =
            Result.success(
                testVolunteerUser.copy(
                    fullName = fullName,
                    phone = phone,
                    description = bio
                )
            )
        override suspend fun uploadAvatar(uriString: String): Result<User> =
            Result.success(testVolunteerUser.copy(avatarUrl = "/uploads/avatar_new.jpg"))
        override suspend fun deleteAvatar(): Result<User> =
            Result.success(testVolunteerUser.copy(avatarUrl = null))
        override fun observeOrganizerEvents(organizerId: String): Flow<List<Event>> = flowOf(emptyList())
    }

    private val fakeApplicationRepository = object : ApplicationRepository {
        override fun observeApplicationsForEvent(eventId: String) = emptyFlow<List<VolunteerApplication>>()
        override fun observeEventApplicationsManage(eventId: String) = emptyFlow<EventApplicationsManage>()
        override suspend fun getEventApplicationsManage(eventId: String): Result<EventApplicationsManage> = Result.failure(UnsupportedOperationException())
        override fun observeMyApplications() = emptyFlow<List<VolunteerApplication>>()
        override suspend fun getMyApplications(): Result<List<VolunteerApplication>> = Result.success(emptyList())
        override suspend fun submitApplication(eventId: String, motivationLetter: String, agreedToShareContacts: Boolean) =
            Result.failure<VolunteerApplication>(UnsupportedOperationException())
        override suspend fun withdrawApplication(applicationId: String): Result<VolunteerApplication> =
            Result.success(
                VolunteerApplication(
                    id = applicationId,
                    eventId = "ev-1",
                    volunteerId = "user-vol-1",
                    status = ApplicationStatus.WITHDRAWN,
                    createdAt = "2026-11-01T10:00:00Z"
                )
            )
        override suspend fun approveApplication(applicationId: String) = Result.failure<VolunteerApplication>(UnsupportedOperationException())
        override suspend fun rejectApplication(applicationId: String, reason: String) = Result.failure<VolunteerApplication>(UnsupportedOperationException())
    }

    private val fakeComplaintRepository = object : ComplaintRepository {
        override suspend fun createComplaint(
            targetType: ComplaintTargetType,
            targetId: String,
            violationCategory: String,
            description: String
        ): Result<Complaint> = Result.success(
            Complaint(
                id = "comp-1",
                reporterId = "user-vol-1",
                reporterName = "Оксана",
                violatorId = targetId,
                violatorName = "Порушник",
                reason = description,
                targetType = targetType.apiValue,
                targetId = targetId,
                violationCategory = violationCategory
            )
        )
    }

    private fun createViewModel(targetUserId: String? = null): ProfileViewModel =
        ProfileViewModel(
            targetUserId = targetUserId,
            getMyProfileUseCase = GetMyProfileUseCase(fakeProfileRepository),
            getUserProfileUseCase = GetUserProfileUseCase(fakeProfileRepository),
            updateProfileUseCase = UpdateProfileUseCase(fakeProfileRepository),
            uploadAvatarUseCase = UploadAvatarUseCase(fakeProfileRepository),
            deleteAvatarUseCase = DeleteAvatarUseCase(fakeProfileRepository),
            withdrawApplicationUseCase = WithdrawApplicationUseCase(fakeApplicationRepository),
            logoutUseCase = LogoutUseCase(fakeAuthRepository),
            observeCurrentUserUseCase = ObserveCurrentUserUseCase(fakeAuthRepository),
            profileRepository = fakeProfileRepository,
            submitComplaintUseCase = SubmitComplaintUseCase(fakeComplaintRepository)
        )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        currentUserFlow.value = testVolunteerUser
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial profile loads current user profile with reviews and tasks`() = runTest {
        val viewModel = createViewModel()
        val collectJob = backgroundScope.launch(testDispatcher) {
            viewModel.uiState.collect {}
        }
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertFalse(state.isGuest)
        assertTrue(state.isCurrentUser)
        assertNotNull(state.user)
        assertEquals("Оксана Петренко", state.user?.fullName)
        assertEquals(1, state.user?.reviews?.size)
        assertEquals(1, state.user?.myTasks?.size)
        assertEquals(4.8, state.user?.rating ?: 0.0, 0.01)
        collectJob.cancel()
    }

    @Test
    fun `initial profile loads guest state when user is not logged in`() = runTest {
        currentUserFlow.value = User(id = "user-guest", role = UserRole.GUEST)
        val viewModel = createViewModel()
        val collectJob = backgroundScope.launch(testDispatcher) {
            viewModel.uiState.collect {}
        }
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.isGuest)
        assertTrue(state.isCurrentUser)
        collectJob.cancel()
    }

    @Test
    fun `initial profile loads public profile when external userId is provided`() = runTest {
        val viewModel = createViewModel("user-external")
        val collectJob = backgroundScope.launch(testDispatcher) {
            viewModel.uiState.collect {}
        }
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertFalse(state.isCurrentUser)
        assertFalse(state.isGuest)
        assertEquals("Іван Організатор", state.user?.fullName)
        collectJob.cancel()
    }

    @Test
    fun `select tab updates selectedTab in state`() = runTest {
        val viewModel = createViewModel()
        val collectJob = backgroundScope.launch(testDispatcher) {
            viewModel.uiState.collect {}
        }
        advanceUntilIdle()

        assertEquals(ProfileTab.INFO, viewModel.uiState.value.selectedTab)
        viewModel.onEvent(ProfileEvent.SelectTab(ProfileTab.REVIEWS))
        advanceUntilIdle()

        assertEquals(ProfileTab.REVIEWS, viewModel.uiState.value.selectedTab)
        collectJob.cancel()
    }

    @Test
    fun `edit profile validates name length and saves changes`() = runTest {
        val viewModel = createViewModel()
        val collectJob = backgroundScope.launch(testDispatcher) {
            viewModel.uiState.collect {}
        }
        advanceUntilIdle()

        viewModel.onEvent(ProfileEvent.EditProfileClicked)
        assertTrue(viewModel.uiState.value.isEditProfileDialogVisible)
        assertEquals("Оксана Петренко", viewModel.uiState.value.editFullName)

        viewModel.onEvent(ProfileEvent.EditFullNameChanged("О"))
        viewModel.onEvent(ProfileEvent.SaveProfileClicked)
        advanceUntilIdle()

        assertEquals(R.string.profile_name_too_short_error, viewModel.uiState.value.errorMessageResId)
        assertTrue(viewModel.uiState.value.isEditProfileDialogVisible)

        viewModel.onEvent(ProfileEvent.EditFullNameChanged("Оксана Нова"))
        viewModel.onEvent(ProfileEvent.SaveProfileClicked)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isEditProfileDialogVisible)
        assertNull(viewModel.uiState.value.errorMessageResId)
        assertEquals("Оксана Нова", viewModel.uiState.value.user?.fullName)
        collectJob.cancel()
    }

    @Test
    fun `confirm withdraw task updates application status`() = runTest {
        val viewModel = createViewModel()
        val collectJob = backgroundScope.launch(testDispatcher) {
            viewModel.uiState.collect {}
        }
        advanceUntilIdle()

        viewModel.onEvent(ProfileEvent.WithdrawTaskClicked("app-1"))
        assertEquals("app-1", viewModel.uiState.value.withdrawConfirmationApplicationId)

        viewModel.onEvent(ProfileEvent.ConfirmWithdrawTaskClicked)
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.withdrawConfirmationApplicationId)
        val updatedTask = viewModel.uiState.value.user?.myTasks?.find { it.applicationId == "app-1" }
        assertNotNull(updatedTask)
        assertEquals(ApplicationStatus.WITHDRAWN, updatedTask?.applicationStatus)
        assertFalse(updatedTask?.canWithdraw ?: true)
        collectJob.cancel()
    }

    @Test
    fun `confirm logout calls logout and navigates back`() = runTest {
        val viewModel = createViewModel()
        val effects = mutableListOf<ProfileEffect>()
        val effectJob = backgroundScope.launch(testDispatcher) {
            viewModel.effect.collect { effects.add(it) }
        }
        val collectJob = backgroundScope.launch(testDispatcher) {
            viewModel.uiState.collect {}
        }
        advanceUntilIdle()

        viewModel.onEvent(ProfileEvent.ConfirmLogoutClicked)
        advanceUntilIdle()

        assertEquals(listOf(ProfileEffect.NavigateBack), effects)
        assertEquals(UserRole.GUEST, currentUserFlow.value.role)
        collectJob.cancel()
        effectJob.cancel()
    }

    @Test
    fun `uploading and deleting avatar updates user avatarUrl in state`() = runTest {
        val viewModel = createViewModel()
        val collectJob = backgroundScope.launch(testDispatcher) {
            viewModel.uiState.collect {}
        }
        advanceUntilIdle()

        viewModel.onEvent(ProfileEvent.AvatarSelected("content://media/external/images/media/10"))
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isUploadingAvatar)
        assertEquals("/uploads/avatar_new.jpg", viewModel.uiState.value.user?.avatarUrl)

        viewModel.onEvent(ProfileEvent.DeleteAvatarClicked)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isUploadingAvatar)
        assertNull(viewModel.uiState.value.user?.avatarUrl)
        collectJob.cancel()
    }

    @Test
    fun `ReportUserClicked sends NavigateToReport effect with user info`() = runTest {
        val viewModel = createViewModel(targetUserId = "other-user-2")
        val effects = mutableListOf<ProfileEffect>()
        val effectJob = backgroundScope.launch(testDispatcher) {
            viewModel.effect.collect { effects.add(it) }
        }
        val collectJob = backgroundScope.launch(testDispatcher) { viewModel.uiState.collect {} }
        advanceUntilIdle()

        viewModel.onEvent(ProfileEvent.ReportUserClicked)
        advanceUntilIdle()

        val reportEffect = effects.filterIsInstance<ProfileEffect.NavigateToReport>().firstOrNull()
        assertNotNull(reportEffect)
        assertEquals(ComplaintTargetType.USER.apiValue, reportEffect?.targetType)
        assertEquals("user-external", reportEffect?.targetId)
        effectJob.cancel()
        collectJob.cancel()
    }
}
