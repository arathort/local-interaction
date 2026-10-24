package com.arathort.localinteraction.domain.model

data class Category(
    val id: String,
    val name: String,
    val boundEventsCount: Int = 0,
    val isArchived: Boolean = false
)
