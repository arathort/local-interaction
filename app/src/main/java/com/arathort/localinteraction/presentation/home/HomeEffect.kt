package com.arathort.localinteraction.presentation.home

sealed interface HomeEffect {
    data class NavigateToEventDetails(val eventId: String) : HomeEffect
    data object NavigateToAuth : HomeEffect
    data object NavigateToProfile : HomeEffect
    data object NavigateToCreateEvent : HomeEffect
    data object NavigateToAdminPanel : HomeEffect
}
