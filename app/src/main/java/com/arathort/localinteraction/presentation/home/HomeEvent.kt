package com.arathort.localinteraction.presentation.home

import com.arathort.localinteraction.domain.model.UserRole

sealed interface HomeEvent {
    data class SearchQueryChanged(val query: String) : HomeEvent
    data class CategoryToggled(val categoryId: String) : HomeEvent
    data object ClearFilters : HomeEvent
    data class ViewModeChanged(val isMapView: Boolean) : HomeEvent
    data class EventClicked(val eventId: String) : HomeEvent
    data class RoleSwitched(val role: UserRole) : HomeEvent
    data object NavigateToAuthClicked : HomeEvent
    data object NavigateToProfileClicked : HomeEvent
    data object NavigateToCreateEventClicked : HomeEvent
    data object NavigateToAdminClicked : HomeEvent
}
