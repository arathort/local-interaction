package com.arathort.localinteraction

import com.arathort.localinteraction.domain.model.Category
import com.arathort.localinteraction.domain.model.Comment
import com.arathort.localinteraction.domain.model.Event
import com.arathort.localinteraction.domain.model.EventStatus
import com.arathort.localinteraction.domain.model.GeoPoint
import com.arathort.localinteraction.domain.model.OrganizerShortInfo
import com.arathort.localinteraction.domain.model.User
import com.arathort.localinteraction.domain.model.UserRole
import com.arathort.localinteraction.domain.repository.AuthRepository
import com.arathort.localinteraction.domain.repository.EventRepository
import com.arathort.localinteraction.domain.usecase.GetOpenEventsUseCase
import com.arathort.localinteraction.domain.usecase.ObserveCategoriesUseCase
import com.arathort.localinteraction.domain.usecase.ObserveCurrentUserUseCase
import com.arathort.localinteraction.presentation.home.HomeEffect
import com.arathort.localinteraction.presentation.home.HomeEvent
import com.arathort.localinteraction.presentation.home.HomeViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
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
class HomeViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var viewModel: HomeViewModel

    private val sampleEvents = listOf(
        Event(
            id = "event-1",
            title = "Прибирання парку",
            description = "Толока в парку міста",
            categoryId = "cat-eco",
            categoryName = "Екологія",
            status = EventStatus.OPEN,
            eventDate = "2026-10-31T10:00:00Z",
            location = GeoPoint(50.45, 30.52, "Київ"),
            volunteerQuota = 10,
            approvedVolunteersCount = 2,
            organizer = OrganizerShortInfo(id = "org-1", fullName = "Тарас", rating = 4.8)
        ),
        Event(
            id = "event-2",
            title = "Допомога притулку",
            description = "Корм для тварин",
            categoryId = "cat-animals",
            categoryName = "Тварини",
            status = EventStatus.OPEN,
            eventDate = "2026-11-01T10:00:00Z",
            location = GeoPoint(50.46, 30.53, "Київ"),
            volunteerQuota = 5,
            approvedVolunteersCount = 1,
            organizer = OrganizerShortInfo(id = "org-2", fullName = "Олена", rating = 4.9)
        )
    )

    private val sampleCategories = listOf(
        Category(id = "cat-eco", name = "Екологія"),
        Category(id = "cat-animals", name = "Тварини")
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        val fakeEventRepository = object : EventRepository {
            override fun observeOpenEvents(searchQuery: String, categoryIds: Set<String>): Flow<List<Event>> =
                flowOf(sampleEvents).map { list ->
                    list.filter { event ->
                        (categoryIds.isEmpty() || event.categoryId in categoryIds) &&
                            (searchQuery.isBlank() || event.title.contains(searchQuery, ignoreCase = true))
                    }
                }

            override suspend fun getOpenEvents(
                searchQuery: String,
                categoryIds: Set<String>,
                skip: Int,
                limit: Int
            ): Result<List<Event>> {
                val filtered = sampleEvents.filter { event ->
                    (categoryIds.isEmpty() || event.categoryId in categoryIds) &&
                        (searchQuery.isBlank() || event.title.contains(searchQuery, ignoreCase = true))
                }
                return Result.success(filtered.drop(skip).take(limit))
            }

            override fun observeCategories(): Flow<List<Category>> = flowOf(sampleCategories)
            override suspend fun getEventById(eventId: String): Result<Event> =
                sampleEvents.find { it.id == eventId }?.let { Result.success(it) } ?: Result.failure(NoSuchElementException())
            override fun observeCommentsForEvent(eventId: String): Flow<List<Comment>> = flowOf(emptyList())
            override suspend fun addComment(eventId: String, text: String, replyToCommentId: String?): Result<Comment> =
                Result.failure(UnsupportedOperationException())
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
                Result.failure(UnsupportedOperationException())
        }

        val fakeAuthRepository = object : AuthRepository {
            override val currentUser: Flow<User> = flowOf(User(id = "u-1", fullName = "Олена", role = UserRole.VOLUNTEER))
            override suspend fun login(email: String, password: String): Result<User> = Result.success(User(id = "u-1"))
            override suspend fun register(fullName: String, email: String, password: String, role: UserRole, acceptedPrivacyPolicy: Boolean): Result<User> = Result.success(User(id = "u-1"))
            override suspend fun logout(): Result<Unit> = Result.success(Unit)
            override suspend fun restoreSession(): Result<User?> = Result.success(null)
        }

        viewModel = HomeViewModel(
            getOpenEventsUseCase = GetOpenEventsUseCase(fakeEventRepository),
            observeCategoriesUseCase = ObserveCategoriesUseCase(fakeEventRepository),
            observeCurrentUserUseCase = ObserveCurrentUserUseCase(fakeAuthRepository)
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state loads open events categories and volunteer user`() = runTest {
        val collectJob = backgroundScope.launch(testDispatcher) {
            viewModel.state.collect {}
        }
        advanceUntilIdle()

        val currentState = viewModel.state.value
        assertFalse(currentState.isLoading)
        assertEquals(2, currentState.events.size)
        assertEquals(2, currentState.categories.size)
        assertEquals(UserRole.VOLUNTEER, currentState.currentUser.role)

        collectJob.cancel()
    }

    @Test
    fun `filtering by category and search query updates events list and ClearFilters resets`() = runTest {
        val collectJob = backgroundScope.launch(testDispatcher) {
            viewModel.state.collect {}
        }
        advanceUntilIdle()

        viewModel.onEvent(HomeEvent.CategoryToggled("cat-animals"))
        advanceUntilIdle()
        assertEquals(setOf("cat-animals"), viewModel.state.value.selectedCategoryIds)
        assertEquals(1, viewModel.state.value.events.size)

        viewModel.onEvent(HomeEvent.ClearFilters)
        advanceUntilIdle()
        assertTrue(viewModel.state.value.selectedCategoryIds.isEmpty())
        assertEquals(2, viewModel.state.value.events.size)

        collectJob.cancel()
    }

    @Test
    fun `toggling view mode and clicking event emits expected state and effect`() = runTest {
        val collectJob = backgroundScope.launch(testDispatcher) {
            viewModel.state.collect {}
        }
        advanceUntilIdle()

        viewModel.onEvent(HomeEvent.ViewModeChanged(true))
        advanceUntilIdle()
        assertTrue(viewModel.state.value.isMapView)

        viewModel.onEvent(HomeEvent.EventClicked("event-1"))
        advanceUntilIdle()
        val effect = viewModel.effects.first()
        assertEquals(HomeEffect.NavigateToEventDetails("event-1"), effect)

        collectJob.cancel()
    }

    @Test
    fun `selecting and dismissing map marker updates state correctly`() = runTest {
        val collectJob = backgroundScope.launch(testDispatcher) {
            viewModel.state.collect {}
        }
        advanceUntilIdle()

        viewModel.onEvent(HomeEvent.ViewModeChanged(true))
        advanceUntilIdle()
        assertNull(viewModel.state.value.selectedMapEventId)
        assertNull(viewModel.state.value.selectedMapEvent)

        viewModel.onEvent(HomeEvent.MapMarkerClicked("event-1"))
        advanceUntilIdle()
        assertEquals("event-1", viewModel.state.value.selectedMapEventId)
        assertNotNull(viewModel.state.value.selectedMapEvent)
        assertEquals("Прибирання парку", viewModel.state.value.selectedMapEvent?.title)

        viewModel.onEvent(HomeEvent.MapDismissPreviewClicked)
        advanceUntilIdle()
        assertNull(viewModel.state.value.selectedMapEventId)
        assertNull(viewModel.state.value.selectedMapEvent)

        collectJob.cancel()
    }

    @Test
    fun `switching view mode to list clears selected map marker`() = runTest {
        val collectJob = backgroundScope.launch(testDispatcher) {
            viewModel.state.collect {}
        }
        advanceUntilIdle()

        viewModel.onEvent(HomeEvent.ViewModeChanged(true))
        advanceUntilIdle()
        viewModel.onEvent(HomeEvent.MapMarkerClicked("event-2"))
        advanceUntilIdle()
        assertEquals("event-2", viewModel.state.value.selectedMapEventId)

        viewModel.onEvent(HomeEvent.ViewModeChanged(false))
        advanceUntilIdle()
        assertFalse(viewModel.state.value.isMapView)
        assertNull(viewModel.state.value.selectedMapEventId)

        collectJob.cancel()
    }

    @Test
    fun `LoadMoreEvents and Refresh update pagination state`() = runTest {
        val collectJob = backgroundScope.launch(testDispatcher) {
            viewModel.state.collect {}
        }
        advanceUntilIdle()

        viewModel.onEvent(HomeEvent.LoadMoreEvents)
        advanceUntilIdle()

        assertFalse(viewModel.state.value.isPaginationLoading)
        assertEquals(2, viewModel.state.value.events.size)

        viewModel.onEvent(HomeEvent.Refresh)
        advanceUntilIdle()

        assertFalse(viewModel.state.value.isLoading)
        assertEquals(2, viewModel.state.value.events.size)

        collectJob.cancel()
    }
}
