package com.arathort.localinteraction.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arathort.localinteraction.domain.model.Category
import com.arathort.localinteraction.domain.model.Event
import com.arathort.localinteraction.domain.model.User
import com.arathort.localinteraction.domain.usecase.GetOpenEventsUseCase
import com.arathort.localinteraction.domain.usecase.ObserveCategoriesUseCase
import com.arathort.localinteraction.domain.usecase.ObserveCurrentUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getOpenEventsUseCase: GetOpenEventsUseCase,
    observeCategoriesUseCase: ObserveCategoriesUseCase,
    observeCurrentUserUseCase: ObserveCurrentUserUseCase
) : ViewModel() {

    private val pageSize = 20

    private val searchQueryFlow = MutableStateFlow("")
    private val selectedCategoriesFlow = MutableStateFlow<Set<String>>(emptySet())
    private val isMapViewFlow = MutableStateFlow(false)
    private val selectedMapEventIdFlow = MutableStateFlow<String?>(null)

    private val eventsFlow = MutableStateFlow<List<Event>>(emptyList())
    private val isLoadingFlow = MutableStateFlow(true)
    private val isPaginationLoadingFlow = MutableStateFlow(false)
    private val canLoadMoreFlow = MutableStateFlow(true)

    private val effectChannel = Channel<HomeEffect>(Channel.BUFFERED)
    val effects: Flow<HomeEffect> = effectChannel.receiveAsFlow()

    private val extraStateFlow: Flow<HomeExtraState> = combine(
        selectedCategoriesFlow,
        isMapViewFlow,
        selectedMapEventIdFlow,
        isLoadingFlow,
        combine(isPaginationLoadingFlow, canLoadMoreFlow) { paginating, canMore -> paginating to canMore }
    ) { cats, isMap, mapId, loading, (paginating, canMore) ->
        HomeExtraState(
            selectedCategories = cats,
            isMapView = isMap,
            selectedMapId = mapId,
            isLoading = loading,
            isPaginationLoading = paginating,
            canLoadMore = canMore
        )
    }

    val state: StateFlow<HomeState> = combine(
        observeCurrentUserUseCase(),
        eventsFlow,
        observeCategoriesUseCase(),
        searchQueryFlow,
        extraStateFlow
    ) { user, events, categories, query, extra ->
        val validSelectedMapId = if (events.any { it.id == extra.selectedMapId }) extra.selectedMapId else null
        HomeState(
            currentUser = user,
            events = events,
            categories = categories,
            selectedCategoryIds = extra.selectedCategories,
            searchQuery = query,
            isMapView = extra.isMapView,
            selectedMapEventId = validSelectedMapId,
            isLoading = extra.isLoading,
            isPaginationLoading = extra.isPaginationLoading,
            canLoadMore = extra.canLoadMore
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeState()
    )

    init {
        viewModelScope.launch {
            loadInitialPage(searchQueryFlow.value, selectedCategoriesFlow.value)
        }
        viewModelScope.launch {
            searchQueryFlow.drop(1).debounce(300).collectLatest { query ->
                loadInitialPage(query, selectedCategoriesFlow.value)
            }
        }
    }

    private suspend fun loadInitialPage(query: String, categories: Set<String>) {
        isLoadingFlow.value = true
        canLoadMoreFlow.value = true
        val result = getOpenEventsUseCase(
            searchQuery = query,
            categoryIds = categories,
            skip = 0,
            limit = pageSize
        )
        val items = result.getOrDefault(emptyList())
        eventsFlow.value = items
        canLoadMoreFlow.value = items.size >= pageSize
        isLoadingFlow.value = false
    }

    private fun loadMore() {
        if (isPaginationLoadingFlow.value || !canLoadMoreFlow.value || isLoadingFlow.value) return
        viewModelScope.launch {
            isPaginationLoadingFlow.value = true
            val currentSize = eventsFlow.value.size
            val result = getOpenEventsUseCase(
                searchQuery = searchQueryFlow.value,
                categoryIds = selectedCategoriesFlow.value,
                skip = currentSize,
                limit = pageSize
            )
            val items = result.getOrDefault(emptyList())
            eventsFlow.update { current -> (current + items).distinctBy { it.id } }
            canLoadMoreFlow.value = items.size >= pageSize
            isPaginationLoadingFlow.value = false
        }
    }

    private fun refresh() {
        viewModelScope.launch {
            loadInitialPage(searchQueryFlow.value, selectedCategoriesFlow.value)
        }
    }

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
                viewModelScope.launch {
                    loadInitialPage(searchQueryFlow.value, selectedCategoriesFlow.value)
                }
            }
            HomeEvent.ClearFilters -> {
                searchQueryFlow.value = ""
                selectedCategoriesFlow.value = emptySet()
                selectedMapEventIdFlow.value = null
                viewModelScope.launch {
                    loadInitialPage("", emptySet())
                }
            }
            is HomeEvent.ViewModeChanged -> {
                isMapViewFlow.value = event.isMapView
                if (!event.isMapView) {
                    selectedMapEventIdFlow.value = null
                }
            }
            is HomeEvent.MapMarkerClicked -> {
                selectedMapEventIdFlow.value = event.eventId
            }
            HomeEvent.MapDismissPreviewClicked -> {
                selectedMapEventIdFlow.value = null
            }
            HomeEvent.LoadMoreEvents -> loadMore()
            HomeEvent.Refresh -> refresh()
            is HomeEvent.EventClicked -> {
                viewModelScope.launch {
                    effectChannel.send(HomeEffect.NavigateToEventDetails(event.eventId))
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

private data class HomeExtraState(
    val selectedCategories: Set<String>,
    val isMapView: Boolean,
    val selectedMapId: String?,
    val isLoading: Boolean,
    val isPaginationLoading: Boolean,
    val canLoadMore: Boolean
)

