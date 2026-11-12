package com.arathort.localinteraction.presentation.home

import com.arathort.localinteraction.domain.model.Category
import com.arathort.localinteraction.domain.model.Event
import com.arathort.localinteraction.domain.model.User
import com.arathort.localinteraction.domain.model.UserRole

data class HomeState(
    val currentUser: User = User(
        id = "user-guest",
        fullName = "",
        email = "",
        role = UserRole.GUEST
    ),
    val events: List<Event> = emptyList(),
    val categories: List<Category> = emptyList(),
    val selectedCategoryIds: Set<String> = emptySet(),
    val searchQuery: String = "",
    val isMapView: Boolean = false,
    val selectedMapEventId: String? = null,
    val isLoading: Boolean = true,
    val isPaginationLoading: Boolean = false,
    val canLoadMore: Boolean = true
) {
    val selectedMapEvent: Event?
        get() = events.find { it.id == selectedMapEventId }
}
