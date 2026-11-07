package com.arathort.localinteraction

import com.arathort.localinteraction.domain.model.Category
import com.arathort.localinteraction.domain.model.Comment
import com.arathort.localinteraction.domain.model.Event
import com.arathort.localinteraction.domain.model.EventStatus
import com.arathort.localinteraction.domain.model.GeoPoint
import com.arathort.localinteraction.domain.model.User
import com.arathort.localinteraction.domain.model.UserRole
import com.arathort.localinteraction.domain.repository.AuthRepository
import com.arathort.localinteraction.domain.repository.EventRepository
import com.arathort.localinteraction.domain.usecase.CreateEventUseCase
import com.arathort.localinteraction.domain.usecase.ObserveCategoriesUseCase
import com.arathort.localinteraction.domain.usecase.ObserveCurrentUserUseCase
import com.arathort.localinteraction.presentation.create_event.CreateEventEffect
import com.arathort.localinteraction.presentation.create_event.CreateEventEvent
import com.arathort.localinteraction.presentation.create_event.CreateEventViewModel
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
class CreateEventViewModelTest {

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

    private val sampleCategories = listOf(
        Category(id = "cat-eco", name = "Екологія"),
        Category(id = "cat-animals", name = "Тварини")
    )

    private val fakeAuthRepository = object : AuthRepository {
        override val currentUser: Flow<User> = currentUserFlow
        override suspend fun login(email: String, password: String) = Result.success(organizerUser)
        override suspend fun register(fullName: String, email: String, password: String, role: UserRole, acceptedPrivacyPolicy: Boolean) = Result.success(organizerUser)
        override suspend fun logout() = Result.success(Unit)
        override suspend fun restoreSession() = Result.success(organizerUser)
    }

    private val fakeEventRepository = object : EventRepository {
        override fun observeOpenEvents(searchQuery: String, categoryIds: Set<String>) = flowOf(emptyList<Event>())
        override fun observeCategories(): Flow<List<Category>> = flowOf(sampleCategories)
        override suspend fun getEventById(eventId: String) = Result.failure<Event>(UnsupportedOperationException())
        override fun observeCommentsForEvent(eventId: String) = flowOf(emptyList<Comment>())
        override suspend fun addComment(eventId: String, text: String) = Result.failure<Comment>(UnsupportedOperationException())
        override suspend fun createEvent(
            title: String,
            description: String,
            categoryId: String,
            dateTimeMillis: Long,
            address: String,
            location: GeoPoint,
            volunteerQuota: Int,
            photoUrls: List<String>
        ): Result<Event> = Result.success(
            Event(
                id = "created-event-1",
                title = title,
                description = description,
                categoryId = categoryId,
                categoryName = "Екологія",
                eventDate = "2026-11-20T10:00:00Z",
                location = location,
                volunteerQuota = volunteerQuota,
                photos = photoUrls
            )
        )
        override suspend fun updateEventStatus(eventId: String, newStatus: EventStatus) = Result.failure<Event>(UnsupportedOperationException())
    }

    private fun createViewModel(): CreateEventViewModel = CreateEventViewModel(
        createEventUseCase = CreateEventUseCase(fakeEventRepository),
        observeCategoriesUseCase = ObserveCategoriesUseCase(fakeEventRepository),
        observeCurrentUserUseCase = ObserveCurrentUserUseCase(fakeAuthRepository)
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        currentUserFlow.value = organizerUser
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial load populates categories and selected category for organizer`() = runTest {
        val viewModel = createViewModel()
        val collectJob = backgroundScope.launch(testDispatcher) {
            viewModel.uiState.collect {}
        }
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isOrganizer)
        assertFalse(state.isLoadingCategories)
        assertEquals(2, state.categories.size)
        assertEquals("cat-eco", state.selectedCategoryId)
        collectJob.cancel()
    }

    @Test
    fun `non-organizer user triggers role error and back navigation`() = runTest {
        currentUserFlow.value = volunteerUser
        val viewModel = createViewModel()
        val effects = mutableListOf<CreateEventEffect>()
        val effectJob = backgroundScope.launch(testDispatcher) {
            viewModel.effect.collect { effects.add(it) }
        }
        val collectJob = backgroundScope.launch(testDispatcher) {
            viewModel.uiState.collect {}
        }
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isOrganizer)
        assertTrue(effects.contains(CreateEventEffect.ShowSnackbar(R.string.error_only_organizers_can_create_events)))
        assertTrue(effects.contains(CreateEventEffect.NavigateBack))
        collectJob.cancel()
        effectJob.cancel()
    }

    @Test
    fun `validation fails when title is too short`() = runTest {
        val viewModel = createViewModel()
        val collectJob = backgroundScope.launch(testDispatcher) {
            viewModel.uiState.collect {}
        }
        advanceUntilIdle()

        viewModel.onEvent(CreateEventEvent.TitleChanged("Еко"))
        viewModel.onEvent(CreateEventEvent.SubmitClicked)
        advanceUntilIdle()

        assertEquals(R.string.error_event_title_length, viewModel.uiState.value.titleErrorResId)
        collectJob.cancel()
    }

    @Test
    fun `validation fails when description is less than 50 characters`() = runTest {
        val viewModel = createViewModel()
        val collectJob = backgroundScope.launch(testDispatcher) {
            viewModel.uiState.collect {}
        }
        advanceUntilIdle()

        viewModel.onEvent(CreateEventEvent.TitleChanged("Толока у парку"))
        viewModel.onEvent(CreateEventEvent.DescriptionChanged("Дуже короткий опис"))
        viewModel.onEvent(CreateEventEvent.SubmitClicked)
        advanceUntilIdle()

        assertEquals(R.string.error_event_description_too_short, viewModel.uiState.value.descriptionErrorResId)
        collectJob.cancel()
    }

    @Test
    fun `validation fails when date is in the past or missing`() = runTest {
        val viewModel = createViewModel()
        val collectJob = backgroundScope.launch(testDispatcher) {
            viewModel.uiState.collect {}
        }
        advanceUntilIdle()

        viewModel.onEvent(CreateEventEvent.TitleChanged("Толока у парку"))
        viewModel.onEvent(CreateEventEvent.DescriptionChanged("Цей опис точно містить більше ніж п'ятдесят символів для проходження валідації форми."))
        viewModel.onEvent(CreateEventEvent.DateTimeSelected(System.currentTimeMillis() - 100000L))
        viewModel.onEvent(CreateEventEvent.SubmitClicked)
        advanceUntilIdle()

        assertEquals(R.string.error_event_date_in_past, viewModel.uiState.value.dateErrorResId)
        collectJob.cancel()
    }

    @Test
    fun `validation fails when address is too short`() = runTest {
        val viewModel = createViewModel()
        val collectJob = backgroundScope.launch(testDispatcher) {
            viewModel.uiState.collect {}
        }
        advanceUntilIdle()

        viewModel.onEvent(CreateEventEvent.TitleChanged("Толока у парку"))
        viewModel.onEvent(CreateEventEvent.DescriptionChanged("Цей опис точно містить більше ніж п'ятдесят символів для проходження валідації форми."))
        viewModel.onEvent(CreateEventEvent.DateTimeSelected(System.currentTimeMillis() + 86400000L))
        viewModel.onEvent(CreateEventEvent.AddressChanged("вул"))
        viewModel.onEvent(CreateEventEvent.SubmitClicked)
        advanceUntilIdle()

        assertEquals(R.string.error_event_address_too_short, viewModel.uiState.value.addressErrorResId)
        collectJob.cancel()
    }

    @Test
    fun `validation fails when quota is zero or negative`() = runTest {
        val viewModel = createViewModel()
        val collectJob = backgroundScope.launch(testDispatcher) {
            viewModel.uiState.collect {}
        }
        advanceUntilIdle()

        viewModel.onEvent(CreateEventEvent.TitleChanged("Толока у парку"))
        viewModel.onEvent(CreateEventEvent.DescriptionChanged("Цей опис точно містить більше ніж п'ятдесят символів для проходження валідації форми."))
        viewModel.onEvent(CreateEventEvent.DateTimeSelected(System.currentTimeMillis() + 86400000L))
        viewModel.onEvent(CreateEventEvent.AddressChanged("вул. Хрещатик, 1"))
        viewModel.onEvent(CreateEventEvent.QuotaChanged("0"))
        viewModel.onEvent(CreateEventEvent.SubmitClicked)
        advanceUntilIdle()

        assertEquals(R.string.error_event_quota_invalid, viewModel.uiState.value.quotaErrorResId)
        collectJob.cancel()
    }

    @Test
    fun `map point selection and confirmation updates state coordinates`() = runTest {
        val viewModel = createViewModel()
        val collectJob = backgroundScope.launch(testDispatcher) {
            viewModel.uiState.collect {}
        }
        advanceUntilIdle()

        viewModel.onEvent(CreateEventEvent.OpenMapPickerClicked)
        assertTrue(viewModel.uiState.value.showMapPickerDialog)

        viewModel.onEvent(CreateEventEvent.MapPointClicked(50.4600, 30.5300))
        assertEquals(50.4600, viewModel.uiState.value.tempMapLatitude, 0.0001)
        assertEquals(30.5300, viewModel.uiState.value.tempMapLongitude, 0.0001)

        viewModel.onEvent(CreateEventEvent.ConfirmMapCoordinatesClicked)
        assertFalse(viewModel.uiState.value.showMapPickerDialog)
        assertEquals(50.4600, viewModel.uiState.value.latitude, 0.0001)
        assertEquals(30.5300, viewModel.uiState.value.longitude, 0.0001)
        collectJob.cancel()
    }

    @Test
    fun `photo urls can be added and removed`() = runTest {
        val viewModel = createViewModel()
        val collectJob = backgroundScope.launch(testDispatcher) {
            viewModel.uiState.collect {}
        }
        advanceUntilIdle()

        viewModel.onEvent(CreateEventEvent.PhotoUrlInputChanged("https://example.com/park.jpg"))
        viewModel.onEvent(CreateEventEvent.AddPhotoUrlClicked)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.photoUrls.contains("https://example.com/park.jpg"))
        assertEquals("", viewModel.uiState.value.photoUrlInput)

        viewModel.onEvent(CreateEventEvent.RemovePhotoClicked("https://example.com/park.jpg"))
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.photoUrls.contains("https://example.com/park.jpg"))
        collectJob.cancel()
    }

    @Test
    fun `successful submission calls createEvent and emits NavigateBack with success snackbar`() = runTest {
        val viewModel = createViewModel()
        val effects = mutableListOf<CreateEventEffect>()
        val effectJob = backgroundScope.launch(testDispatcher) {
            viewModel.effect.collect { effects.add(it) }
        }
        val collectJob = backgroundScope.launch(testDispatcher) {
            viewModel.uiState.collect {}
        }
        advanceUntilIdle()

        viewModel.onEvent(CreateEventEvent.TitleChanged("Прибирання парку Шевченка"))
        viewModel.onEvent(CreateEventEvent.DescriptionChanged("Це детальний опис заходу з прибирання парку, що містить понад п'ятдесят символів для успішного проходження валідації."))
        viewModel.onEvent(CreateEventEvent.CategorySelected("cat-eco"))
        viewModel.onEvent(CreateEventEvent.DateTimeSelected(System.currentTimeMillis() + 86400000L))
        viewModel.onEvent(CreateEventEvent.AddressChanged("вул. Володимирська, 60"))
        viewModel.onEvent(CreateEventEvent.QuotaChanged("10"))
        viewModel.onEvent(CreateEventEvent.SubmitClicked)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isSubmitting)
        assertTrue(effects.contains(CreateEventEffect.ShowSnackbar(R.string.event_create_success)))
        assertTrue(effects.contains(CreateEventEffect.NavigateBack))
        collectJob.cancel()
        effectJob.cancel()
    }
}
