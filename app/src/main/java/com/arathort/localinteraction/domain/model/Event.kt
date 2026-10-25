package com.arathort.localinteraction.domain.model

data class Event(
    val id: String,
    val title: String,
    val description: String,
    val categoryId: String,
    val categoryName: String,
    val status: EventStatus,
    val dateTimeMillis: Long,
    val address: String,
    val location: GeoPoint,
    val volunteerQuota: Int,
    val approvedCount: Int = 0,
    val photoUrls: List<String> = emptyList(),
    val organizerId: String,
    val organizerName: String,
    val organizerAvatarUrl: String? = null,
    val organizerRating: Double = 0.0,
    val organizerContactPhone: String? = null,
    val rejectionReason: String? = null
)
