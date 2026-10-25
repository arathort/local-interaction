package com.arathort.localinteraction

import com.arathort.localinteraction.data.datasource.mock.MockLocalInteractionDataSource
import com.arathort.localinteraction.data.datasource.remote.JwtTokenManager
import com.arathort.localinteraction.data.repository.AuthRepositoryImpl
import com.arathort.localinteraction.data.repository.EventRepositoryImpl
import com.arathort.localinteraction.domain.model.UserRole
import com.arathort.localinteraction.domain.usecase.ObserveCategoriesUseCase
import com.arathort.localinteraction.domain.usecase.ObserveCurrentUserUseCase
import com.arathort.localinteraction.domain.usecase.ObserveOpenEventsUseCase
import com.arathort.localinteraction.domain.usecase.SwitchDemoRoleUseCase
import com.arathort.localinteraction.presentation.home.HomeEffect
import com.arathort.localinteraction.presentation.home.HomeEvent
import com.arathort.localinteraction.presentation.home.HomeViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var viewModel: HomeViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        val dataSource = MockLocalInteractionDataSource(JwtTokenManager()).apply {
            simulatedDelayMs = 0L
        }
        val authRepository = AuthRepositoryImpl(dataSource)
        val eventRepository = EventRepositoryImpl(dataSource)
        viewModel = HomeViewModel(
            observeOpenEventsUseCase = ObserveOpenEventsUseCase(eventRepository),
            observeCategoriesUseCase = ObserveCategoriesUseCase(eventRepository),
            observeCurrentUserUseCase = ObserveCurrentUserUseCase(authRepository),
            switchDemoRoleUseCase = SwitchDemoRoleUseCase(authRepository)
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

        val currentState = viewModel.state.value
        assertFalse(currentState.isLoading)
        assertEquals(3, currentState.events.size)
        assertEquals(5, currentState.categories.size)
        assertEquals(UserRole.VOLUNTEER, currentState.currentUser.role)

        collectJob.cancel()
    }

    @Test
    fun `filtering by category and search query updates events list and ClearFilters resets`() = runTest {
        val collectJob = backgroundScope.launch(testDispatcher) {
            viewModel.state.collect {}
        }

        viewModel.onEvent(HomeEvent.CategoryToggled("cat-animals"))
        assertEquals(setOf("cat-animals"), viewModel.state.value.selectedCategoryIds)
        assertEquals(1, viewModel.state.value.events.size)

        viewModel.onEvent(HomeEvent.ClearFilters)
        assertTrue(viewModel.state.value.selectedCategoryIds.isEmpty())
        assertEquals(3, viewModel.state.value.events.size)

        collectJob.cancel()
    }

    @Test
    fun `toggling view mode and clicking event emits expected state and effect`() = runTest {
        val collectJob = backgroundScope.launch(testDispatcher) {
            viewModel.state.collect {}
        }

        viewModel.onEvent(HomeEvent.ViewModeChanged(true))
        assertTrue(viewModel.state.value.isMapView)

        viewModel.onEvent(HomeEvent.EventClicked("event-1"))
        val effect = viewModel.effects.first()
        assertEquals(HomeEffect.NavigateToEventDetails("event-1"), effect)

        collectJob.cancel()
    }
}
