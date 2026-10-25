package com.arathort.localinteraction.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CategoryDto(
    @SerialName("id") val id: String,
    @SerialName("name") val name: String,
    @SerialName("bound_events_count") val boundEventsCount: Int = 0,
    @SerialName("is_archived") val isArchived: Boolean = false
)
