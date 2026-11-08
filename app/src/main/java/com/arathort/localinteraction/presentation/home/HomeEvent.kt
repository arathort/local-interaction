package com.arathort.localinteraction.presentation.home

sealed interface HomeEvent {
    data class SearchQueryChanged(val query: String) : HomeEvent
    data class CategoryToggled(val categoryId: String) : HomeEvent
    data object ClearFilters : HomeEvent
    data class ViewModeChanged(val isMapView: Boolean) : HomeEvent
    data class MapMarkerClicked(val eventId: String) : HomeEvent
    data object MapDismissPreviewClicked : HomeEvent
    data class EventClicked(val eventId: String) : HomeEvent
    data object NavigateToAuthClicked : HomeEvent
    data object NavigateToProfileClicked : HomeEvent
    data object NavigateToCreateEventClicked : HomeEvent
    data object NavigateToAdminClicked : HomeEvent
}
