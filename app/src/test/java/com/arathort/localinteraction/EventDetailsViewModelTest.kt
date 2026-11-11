package com.arathort.localinteraction

import com.arathort.localinteraction.domain.model.ApplicationStatus
import com.arathort.localinteraction.domain.model.Comment
import com.arathort.localinteraction.domain.model.Complaint
import com.arathort.localinteraction.domain.model.ComplaintTargetType
import com.arathort.localinteraction.domain.model.Event
import com.arathort.localinteraction.domain.model.EventApplicationsManage
import com.arathort.localinteraction.domain.model.EventStatus
import com.arathort.localinteraction.domain.model.GeoPoint
import com.arathort.localinteraction.domain.model.OrganizerShortInfo
import com.arathort.localinteraction.domain.model.User
import com.arathort.localinteraction.domain.model.UserRole
import com.arathort.localinteraction.domain.model.VolunteerApplication
import com.arathort.localinteraction.domain.repository.ApplicationRepository
import com.arathort.localinteraction.domain.repository.AuthRepository
import com.arathort.localinteraction.domain.repository.ComplaintRepository
import com.arathort.localinteraction.domain.repository.EventRepository
import com.arathort.localinteraction.domain.usecase.AddEventCommentUseCase
import com.arathort.localinteraction.domain.usecase.DeleteEventCommentUseCase
import com.arathort.localinteraction.domain.usecase.DeleteEventUseCase
import com.arathort.localinteraction.domain.usecase.GetEventCommentsUseCase
import com.arathort.localinteraction.domain.usecase.GetEventDetailsUseCase
import com.arathort.localinteraction.domain.usecase.GetMyApplicationsUseCase
import com.arathort.localinteraction.domain.usecase.ObserveCurrentUserUseCase
import com.arathort.localinteraction.domain.usecase.SubmitApplicationUseCase
import com.arathort.localinteraction.domain.usecase.SubmitComplaintUseCase
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

    private val initialComment = Comment(
        id = "comment-1",
        eventId = "event-1",
        authorId = "user-organizer-1",
        authorName = "Тарас",
        authorRole = UserRole.ORGANIZER.apiValue,
        text = "Чекаємо на всіх небайдужих!",
        createdAt = "2026-10-30T12:00:00Z"
    )

    private val storedComments = mutableListOf(initialComment)

    private val fakeEventRepository = object : EventRepository {
        override fun observeOpenEvents(searchQuery: String, categoryIds: Set<String>): Flow<List<Event>> = emptyFlow()
        override fun observeCategories() = emptyFlow<List<com.arathort.localinteraction.domain.model.Category>>()
        override suspend fun getEventById(eventId: String): Result<Event> = Result.success(testEvent)
        override fun observeCommentsForEvent(eventId: String): Flow<List<Comment>> = flowOf(storedComments.toList())
        override suspend fun getEventComments(eventId: String, skip: Int, limit: Int): Result<List<Comment>> = Result.success(storedComments.toList())
        override suspend fun addComment(eventId: String, text: String, replyToCommentId: String?): Result<Comment> {
            val user = currentUserFlow.value
            val created = Comment(
                id = "comment-${storedComments.size + 1}",
                eventId = eventId,
                authorId = user.id,
                authorName = user.fullName.orEmpty().ifBlank { "Волонтер" },
                authorRole = user.role.apiValue,
                text = text,
                replyToCommentId = replyToCommentId,
                createdAt = "2026-10-31T08:00:00Z"
            )
            storedComments.add(created)
            return Result.success(created)
        }
        override suspend fun deleteComment(commentId: String): Result<Unit> {
            storedComments.removeAll { it.id == commentId }
            return Result.success(Unit)
        }
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
        override fun observeEventApplicationsManage(eventId: String) = emptyFlow<EventApplicationsManage>()
        override suspend fun getEventApplicationsManage(eventId: String): Result<EventApplicationsManage> = Result.failure(UnsupportedOperationException())
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

    private val fakeComplaintRepository = object : ComplaintRepository {
        override suspend fun createComplaint(
            targetType: ComplaintTargetType,
            targetId: String,
            violationCategory: String,
            description: String
        ): Result<Complaint> = Result.success(
            Complaint(
                id = "complaint-1",
                reporterId = "user-1",
                reporterName = "Волонтер",
                violatorId = "user-organizer-1",
                violatorName = "Тарас",
                reason = description,
                targetType = targetType.apiValue,
                targetId = targetId,
                violationCategory = violationCategory
            )
        )
    }

    private fun createViewModel(eventId: String = "event-1"): EventDetailsViewModel = EventDetailsViewModel(
        eventId = eventId,
        getEventDetailsUseCase = GetEventDetailsUseCase(fakeEventRepository),
        observeCurrentUserUseCase = ObserveCurrentUserUseCase(fakeAuthRepository),
        getMyApplicationsUseCase = GetMyApplicationsUseCase(fakeApplicationRepository),
        submitApplicationUseCase = SubmitApplicationUseCase(fakeApplicationRepository),
        withdrawApplicationUseCase = WithdrawApplicationUseCase(fakeApplicationRepository),
        updateEventStatusUseCase = UpdateEventStatusUseCase(fakeEventRepository),
        getEventCommentsUseCase = GetEventCommentsUseCase(fakeEventRepository),
        addEventCommentUseCase = AddEventCommentUseCase(fakeEventRepository),
        deleteEventCommentUseCase = DeleteEventCommentUseCase(fakeEventRepository),
        deleteEventUseCase = DeleteEventUseCase(fakeEventRepository),
        submitComplaintUseCase = SubmitComplaintUseCase(fakeComplaintRepository)
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        currentUserFlow.value = User(id = "user-guest", role = UserRole.GUEST)
        storedComments.clear()
        storedComments.add(initialComment)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial load fetches event details and comments and sets loading false`() = runTest {
        val viewModel = createViewModel()
        val state = viewModel.uiState.value

        assertFalse(state.isLoading)
        assertNotNull(state.event)
        assertEquals("event-1", state.event?.id)
        assertEquals("Прибирання парку", state.event?.title)
        assertEquals(1, state.comments.size)
        assertEquals("comment-1", state.comments.first().id)
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

    @Test
    fun `Authenticated user submitting comment adds it to comments and clears input`() = runTest {
        currentUserFlow.value = User(id = "user-volunteer-1", fullName = "Олена", role = UserRole.VOLUNTEER)
        val viewModel = createViewModel()

        viewModel.onEvent(EventDetailsEvent.CommentTextChanged("Чи потрібно брати власні рукавички?"))
        viewModel.onEvent(EventDetailsEvent.SubmitCommentClicked)

        val state = viewModel.uiState.value
        assertEquals("", state.commentText)
        assertNull(state.commentErrorResId)
        assertEquals(2, state.comments.size)
        assertEquals("Чи потрібно брати власні рукавички?", state.comments.last().text)
        assertNull(state.comments.last().replyToCommentId)
    }

    @Test
    fun `Replying to comment attaches replyToCommentId and clears replyingToComment after submit`() = runTest {
        currentUserFlow.value = User(id = "user-organizer-1", fullName = "Тарас", role = UserRole.ORGANIZER)
        val viewModel = createViewModel()

        viewModel.onEvent(EventDetailsEvent.ReplyToCommentClicked(initialComment))
        assertEquals(initialComment, viewModel.uiState.value.replyingToComment)

        viewModel.onEvent(EventDetailsEvent.CommentTextChanged("Рукавички видамо на місці!"))
        viewModel.onEvent(EventDetailsEvent.SubmitCommentClicked)

        val state = viewModel.uiState.value
        assertNull(state.replyingToComment)
        assertEquals(2, state.comments.size)
        assertEquals("comment-1", state.comments.last().replyToCommentId)
    }

    @Test
    fun `Submitting empty comment sets commentErrorResId`() = runTest {
        currentUserFlow.value = User(id = "user-volunteer-1", role = UserRole.VOLUNTEER)
        val viewModel = createViewModel()

        viewModel.onEvent(EventDetailsEvent.CommentTextChanged("   "))
        viewModel.onEvent(EventDetailsEvent.SubmitCommentClicked)

        assertEquals(R.string.error_comment_empty, viewModel.uiState.value.commentErrorResId)
        assertEquals(1, viewModel.uiState.value.comments.size)
    }

    @Test
    fun `Guest clicking ReplyToCommentClicked emits NavigateToAuth`() = runTest {
        val viewModel = createViewModel()
        viewModel.onEvent(EventDetailsEvent.ReplyToCommentClicked(initialComment))

        val effect = viewModel.effect.first()
        assertEquals(EventDetailsEffect.NavigateToAuth, effect)
    }

    @Test
    fun `Deleting comment removes it from comments list`() = runTest {
        currentUserFlow.value = User(id = "user-organizer-1", role = UserRole.ORGANIZER)
        val viewModel = createViewModel()

        viewModel.onEvent(EventDetailsEvent.DeleteCommentClicked("comment-1"))
        assertEquals("comment-1", viewModel.uiState.value.deletingCommentId)

        viewModel.onEvent(EventDetailsEvent.ConfirmDeleteComment)

        val state = viewModel.uiState.value
        assertNull(state.deletingCommentId)
        assertTrue(state.comments.isEmpty())
    }

    @Test
    fun `EditEventClicked emits NavigateToEditEvent effect`() = runTest {
        currentUserFlow.value = User(id = "user-organizer-1", role = UserRole.ORGANIZER)
        val viewModel = createViewModel()

        viewModel.onEvent(EventDetailsEvent.EditEventClicked)

        val effect = viewModel.effect.first()
        assertEquals(EventDetailsEffect.NavigateToEditEvent("event-1"), effect)
    }

    @Test
    fun `Deleting event closes dialog and emits ShowSnackbar and NavigateBack`() = runTest {
        currentUserFlow.value = User(id = "user-organizer-1", role = UserRole.ORGANIZER)
        val viewModel = createViewModel()

        viewModel.onEvent(EventDetailsEvent.DeleteEventClicked)
        assertTrue(viewModel.uiState.value.showDeleteEventDialog)

        viewModel.onEvent(EventDetailsEvent.ConfirmDeleteEvent)

        assertFalse(viewModel.uiState.value.showDeleteEventDialog)
        val firstEffect = viewModel.effect.first()
        assertEquals(EventDetailsEffect.ShowSnackbar(R.string.event_delete_success), firstEffect)
    }

    @Test
    fun `ReportEventClicked sends NavigateToReport effect with event info`() = runTest {
        val viewModel = createViewModel()
        val effects = mutableListOf<EventDetailsEffect>()
        val effectJob = backgroundScope.launch(testDispatcher) {
            viewModel.effect.collect { effects.add(it) }
        }
        advanceUntilIdle()

        viewModel.onEvent(EventDetailsEvent.ReportEventClicked)
        advanceUntilIdle()

        val reportEffect = effects.filterIsInstance<EventDetailsEffect.NavigateToReport>().firstOrNull()
        assertNotNull(reportEffect)
        assertEquals(ComplaintTargetType.EVENT.apiValue, reportEffect?.targetType)
        assertEquals("event-1", reportEffect?.targetId)
        assertEquals("Прибирання парку", reportEffect?.targetTitle)
        effectJob.cancel()
    }

    @Test
    fun `ReportCommentClicked sends NavigateToReport effect with comment info`() = runTest {
        val viewModel = createViewModel()
        val effects = mutableListOf<EventDetailsEffect>()
        val effectJob = backgroundScope.launch(testDispatcher) {
            viewModel.effect.collect { effects.add(it) }
        }
        advanceUntilIdle()

        viewModel.onEvent(EventDetailsEvent.ReportCommentClicked("comment-1", "Тарас"))
        advanceUntilIdle()

        val reportEffect = effects.filterIsInstance<EventDetailsEffect.NavigateToReport>().firstOrNull()
        assertNotNull(reportEffect)
        assertEquals(ComplaintTargetType.COMMENT.apiValue, reportEffect?.targetType)
        assertEquals("comment-1", reportEffect?.targetId)
        assertEquals("Тарас", reportEffect?.targetTitle)
        effectJob.cancel()
    }
}
