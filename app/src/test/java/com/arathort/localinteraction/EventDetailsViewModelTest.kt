package com.arathort.localinteraction

import androidx.lifecycle.SavedStateHandle
import com.arathort.localinteraction.domain.model.ApplicationStatus
import com.arathort.localinteraction.domain.model.Comment
import com.arathort.localinteraction.domain.model.Event
import com.arathort.localinteraction.domain.model.EventStatus
import com.arathort.localinteraction.domain.model.GeoPoint
import com.arathort.localinteraction.domain.model.OrganizerShortInfo
import com.arathort.localinteraction.domain.model.User
import com.arathort.localinteraction.domain.model.UserRole
import com.arathort.localinteraction.domain.model.VolunteerApplication
import com.arathort.localinteraction.domain.repository.ApplicationRepository
import com.arathort.localinteraction.domain.repository.AuthRepository
import com.arathort.localinteraction.domain.repository.EventRepository
import com.arathort.localinteraction.domain.usecase.GetEventDetailsUseCase
import com.arathort.localinteraction.domain.usecase.GetMyApplicationsUseCase
import com.arathort.localinteraction.domain.usecase.ObserveCurrentUserUseCase
import com.arathort.localinteraction.domain.usecase.SubmitApplicationUseCase
import com.arathort.localinteraction.domain.usecase.UpdateEventStatusUseCase
import com.arathort.localinteraction.domain.usecase.WithdrawApplicationUseCase
import com.arathort.localinteraction.presentation.event_details.EventDetailsEffect
import com.arathort.localinteraction.presentation.event_details.EventDetailsEvent
import com.arathort.localinteraction.presentation.event_details.EventDetailsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EventDetailsViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val currentUserFlow = MutableStateFlow(User(id = "user-guest", role = UserRole.GUEST))

    private val testEvent = Event(
        id = "event-1",
        title = "Прибирання парку",
        description = "Детальний опис заходу для волонтерів на понад 50 символів.",
        categoryId = "cat-eco",
        categoryName = "Екологія",
        status = EventStatus.OPEN,
        eventDate = "2026-10-31T10:00:00Z",
        location = GeoPoint(50.45, 30.52, "Київ"),
        volunteerQuota = 15,
        approvedVolunteersCount = 3,
        organizer = OrganizerShortInfo(id = "user-organizer-1", fullName = "Тарас", rating = 4.8)
    )

    private val fakeEventRepository = object : EventRepository {
        override fun observeOpenEvents(searchQuery: String, categoryIds: Set<String>): Flow<List<Event>> = emptyFlow()
        override fun observeCategories() = emptyFlow<List<com.arathort.localinteraction.domain.model.Category>>()
        override suspend fun getEventById(eventId: String): Result<Event> = Result.success(testEvent)
        override fun observeCommentsForEvent(eventId: String): Flow<List<Comment>> = flowOf(emptyList())
        override suspend fun addComment(eventId: String, text: String): Result<Comment> = Result.failure(UnsupportedOperationException())
        override suspend fun createEvent(
            title: String,
            description: String,
            categoryId: String,
            dateTimeMillis: Long,
            address: String,
            location: GeoPoint,
            volunteerQuota: Int,
            photoUrls: List<String>
        ): Result<Event> = Result.failure(UnsupportedOperationException())
        override suspend fun updateEventStatus(eventId: String, newStatus: EventStatus): Result<Event> =
            Result.success(testEvent.copy(status = newStatus))
    }

    private val fakeApplicationRepository = object : ApplicationRepository {
        override fun observeApplicationsForEvent(eventId: String) = emptyFlow<List<VolunteerApplication>>()
        override fun observeMyApplications() = emptyFlow<List<VolunteerApplication>>()
        override suspend fun getMyApplications(): Result<List<VolunteerApplication>> = Result.success(emptyList())
        override suspend fun submitApplication(eventId: String, motivationLetter: String, agreedToShareContacts: Boolean): Result<VolunteerApplication> =
            Result.success(
                VolunteerApplication(
                    id = "app-1",
                    eventId = eventId,
                    volunteerId = "user-volunteer-1",
                    status = ApplicationStatus.PENDING,
                    createdAt = "2026-10-31T10:00:00Z"
                )
            )
        override suspend fun withdrawApplication(applicationId: String): Result<VolunteerApplication> =
            Result.success(
                VolunteerApplication(
                    id = applicationId,
                    eventId = "event-1",
                    volunteerId = "user-volunteer-1",
                    status = ApplicationStatus.WITHDRAWN,
                    createdAt = "2026-10-31T10:00:00Z"
                )
            )
        override suspend fun approveApplication(applicationId: String) = Result.failure<VolunteerApplication>(UnsupportedOperationException())
        override suspend fun rejectApplication(applicationId: String, reason: String) = Result.failure<VolunteerApplication>(UnsupportedOperationException())
    }

    private val fakeAuthRepository = object : AuthRepository {
        override val currentUser: Flow<User> = currentUserFlow
        override suspend fun login(email: String, password: String) = Result.success(User(id = "u-1"))
        override suspend fun register(fullName: String, email: String, password: String, role: UserRole, acceptedPrivacyPolicy: Boolean) = Result.success(User(id = "u-1"))
        override suspend fun logout() = Result.success(Unit)
        override suspend fun restoreSession() = Result.success(null)
    }

    private fun createViewModel(): EventDetailsViewModel = EventDetailsViewModel(
        savedStateHandle = SavedStateHandle(mapOf("eventId" to "event-1")),
        getEventDetailsUseCase = GetEventDetailsUseCase(fakeEventRepository),
        observeCurrentUserUseCase = ObserveCurrentUserUseCase(fakeAuthRepository),
        getMyApplicationsUseCase = GetMyApplicationsUseCase(fakeApplicationRepository),
        submitApplicationUseCase = SubmitApplicationUseCase(fakeApplicationRepository),
        withdrawApplicationUseCase = WithdrawApplicationUseCase(fakeApplicationRepository),
        updateEventStatusUseCase = UpdateEventStatusUseCase(fakeEventRepository)
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        currentUserFlow.value = User(id = "user-guest", role = UserRole.GUEST)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial load fetches event details and sets loading false`() = runTest {
        val viewModel = createViewModel()
        val state = viewModel.uiState.value

        assertFalse(state.isLoading)
        assertNotNull(state.event)
        assertEquals("event-1", state.event?.id)
        assertEquals("Прибирання парку", state.event?.title)
    }

    @Test
    fun `Guest clicking Apply emits NavigateToAuth`() = runTest {
        val viewModel = createViewModel()
        viewModel.onEvent(EventDetailsEvent.ApplyClicked)

        val effect = viewModel.effect.first()
        assertEquals(EventDetailsEffect.NavigateToAuth, effect)
    }

    @Test
    fun `Volunteer submitting application updates userApplication`() = runTest {
        currentUserFlow.value = User(id = "user-volunteer-1", role = UserRole.VOLUNTEER)
        val viewModel = createViewModel()

        viewModel.onEvent(EventDetailsEvent.ApplyClicked)
        assertTrue(viewModel.uiState.value.showApplyDialog)

        viewModel.onEvent(EventDetailsEvent.MotivationLetterChanged("Хочу допомогти"))
        viewModel.onEvent(EventDetailsEvent.AgreedToShareContactsChanged(true))
        viewModel.onEvent(EventDetailsEvent.SubmitApplication)

        assertFalse(viewModel.uiState.value.showApplyDialog)
        assertNotNull(viewModel.uiState.value.userApplication)
        assertEquals(ApplicationStatus.PENDING, viewModel.uiState.value.userApplication?.status)
    }

    @Test
    fun `Organizer changing event status updates event in state`() = runTest {
        currentUserFlow.value = User(id = "user-organizer-1", role = UserRole.ORGANIZER)
        val viewModel = createViewModel()

        viewModel.onEvent(EventDetailsEvent.ChangeStatusClicked)
        assertTrue(viewModel.uiState.value.showStatusDialog)

        viewModel.onEvent(EventDetailsEvent.NewStatusSelected(EventStatus.IN_PROGRESS))
        viewModel.onEvent(EventDetailsEvent.SubmitStatusChange)

        assertFalse(viewModel.uiState.value.showStatusDialog)
        assertEquals(EventStatus.IN_PROGRESS, viewModel.uiState.value.event?.status)
    }

    @Test
    fun `BackClicked emits NavigateBack effect`() = runTest {
        val viewModel = createViewModel()
        viewModel.onEvent(EventDetailsEvent.BackClicked)

        val effect = viewModel.effect.first()
        assertEquals(EventDetailsEffect.NavigateBack, effect)
    }
}
