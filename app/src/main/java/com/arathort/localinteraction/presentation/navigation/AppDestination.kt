package com.arathort.localinteraction.presentation.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface AppDestination : NavKey {

    @Serializable
    data object Home : AppDestination

    @Serializable
    data class EventDetails(val eventId: String) : AppDestination

    @Serializable
    data object Auth : AppDestination

    @Serializable
    data object Profile : AppDestination

    @Serializable
    data class CreateEditEvent(val eventId: String? = null) : AppDestination

    @Serializable
    data class ManageApplications(val eventId: String) : AppDestination

    @Serializable
    data class WriteReview(
        val eventId: String,
        val eventTitle: String,
        val targetUserId: String,
        val targetUserName: String
    ) : AppDestination

    @Serializable
    data object AdminPanel : AppDestination
}
