package com.arathort.localinteraction.presentation.event_details

import androidx.annotation.StringRes
import com.arathort.localinteraction.domain.model.Comment
import com.arathort.localinteraction.domain.model.Event
import com.arathort.localinteraction.domain.model.EventStatus
import com.arathort.localinteraction.domain.model.User
import com.arathort.localinteraction.domain.model.VolunteerApplication

data class EventDetailsState(
    val event: Event? = null,
    val currentUser: User = User(id = "guest"),
    val userApplication: VolunteerApplication? = null,
    val comments: List<Comment> = emptyList(),
    val isLoading: Boolean = true,
    val isActionLoading: Boolean = false,
    val showApplyDialog: Boolean = false,
    val motivationLetter: String = "",
    val agreedToShareContacts: Boolean = false,
    @param:StringRes val applyErrorResId: Int? = null,
    val showStatusDialog: Boolean = false,
    val selectedNewStatus: EventStatus? = null,
    @param:StringRes val errorMessageResId: Int? = null
)
