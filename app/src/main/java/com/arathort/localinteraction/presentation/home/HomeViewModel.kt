package com.arathort.localinteraction.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arathort.localinteraction.domain.usecase.ObserveCategoriesUseCase
import com.arathort.localinteraction.domain.usecase.ObserveCurrentUserUseCase
import com.arathort.localinteraction.domain.usecase.ObserveOpenEventsUseCase
import com.arathort.localinteraction.domain.usecase.SwitchDemoRoleUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    observeOpenEventsUseCase: ObserveOpenEventsUseCase,
    observeCategoriesUseCase: ObserveCategoriesUseCase,
    observeCurrentUserUseCase: ObserveCurrentUserUseCase,
    private val switchDemoRoleUseCase: SwitchDemoRoleUseCase
) : ViewModel() {

    private val searchQueryFlow = MutableStateFlow("")
    private val selectedCategoriesFlow = MutableStateFlow<Set<String>>(emptySet())
    private val isMapViewFlow = MutableStateFlow(false)

    private val effectChannel = Channel<HomeEffect>(Channel.BUFFERED)
    val effects: Flow<HomeEffect> = effectChannel.receiveAsFlow()

    private val filteredEventsFlow = combine(
        searchQueryFlow,
        selectedCategoriesFlow
    ) { query, categoryIds ->
        query to categoryIds
    }.flatMapLatest { (query, categoryIds) ->
        observeOpenEventsUseCase(searchQuery = query, categoryIds = categoryIds)
    }

    val state: StateFlow<HomeState> = combine(
        observeCurrentUserUseCase(),
        filteredEventsFlow,
        observeCategoriesUseCase(),
        searchQueryFlow,
        combine(selectedCategoriesFlow, isMapViewFlow) { cats, isMap -> cats to isMap }
    ) { user, events, categories, query, (selectedCategories, isMapView) ->
        HomeState(
            currentUser = user,
            events = events,
            categories = categories,
            selectedCategoryIds = selectedCategories,
            searchQuery = query,
            isMapView = isMapView,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeState()
    )

    fun onEvent(event: HomeEvent) {
        when (event) {
            is HomeEvent.SearchQueryChanged -> {
                searchQueryFlow.value = event.query
            }
            is HomeEvent.CategoryToggled -> {
                selectedCategoriesFlow.update { current ->
                    if (event.categoryId in current) {
                        current - event.categoryId
                    } else {
                        current + event.categoryId
                    }
                }
            }
            HomeEvent.ClearFilters -> {
                searchQueryFlow.value = ""
                selectedCategoriesFlow.value = emptySet()
            }
            is HomeEvent.ViewModeChanged -> {
                isMapViewFlow.value = event.isMapView
            }
            is HomeEvent.EventClicked -> {
                viewModelScope.launch {
                    effectChannel.send(HomeEffect.NavigateToEventDetails(event.eventId))
                }
            }
            is HomeEvent.RoleSwitched -> {
                viewModelScope.launch {
                    switchDemoRoleUseCase(event.role)
                }
            }
            HomeEvent.NavigateToAuthClicked -> {
                viewModelScope.launch {
                    effectChannel.send(HomeEffect.NavigateToAuth)
                }
            }
            HomeEvent.NavigateToProfileClicked -> {
                viewModelScope.launch {
                    effectChannel.send(HomeEffect.NavigateToProfile)
                }
            }
            HomeEvent.NavigateToCreateEventClicked -> {
                viewModelScope.launch {
                    effectChannel.send(HomeEffect.NavigateToCreateEvent)
                }
            }
            HomeEvent.NavigateToAdminClicked -> {
                viewModelScope.launch {
                    effectChannel.send(HomeEffect.NavigateToAdminPanel)
                }
            }
        }
    }
}
