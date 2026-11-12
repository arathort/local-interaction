package com.arathort.localinteraction.domain.usecase

import com.arathort.localinteraction.domain.model.Event
import com.arathort.localinteraction.domain.model.GeoPoint
import com.arathort.localinteraction.domain.repository.EventRepository
import javax.inject.Inject

class CreateEventUseCase @Inject constructor(
    private val eventRepository: EventRepository
) {
    suspend operator fun invoke(
        title: String,
        description: String,
        categoryId: String,
        dateTimeMillis: Long,
        address: String,
        latitude: Double,
        longitude: Double,
        volunteerQuota: Int,
        photoUrls: List<String> = emptyList()
    ): Result<Event> {
        val trimmedTitle = title.trim()
        if (trimmedTitle.length !in 5..150) {
            return Result.failure(IllegalArgumentException("Title length must be between 5 and 150 characters"))
        }

        val trimmedDescription = description.trim()
        if (trimmedDescription.length < 50) {
            return Result.failure(IllegalArgumentException("Description must contain at least 50 characters"))
        }

        val trimmedCategoryId = categoryId.trim()
        if (trimmedCategoryId.isBlank()) {
            return Result.failure(IllegalArgumentException("Category must be selected"))
        }

        if (dateTimeMillis <= System.currentTimeMillis()) {
            return Result.failure(IllegalArgumentException("Event date must be in the future"))
        }

        val trimmedAddress = address.trim()
        if (trimmedAddress.length < 5) {
            return Result.failure(IllegalArgumentException("Address must contain at least 5 characters"))
        }

        if (volunteerQuota <= 0) {
            return Result.failure(IllegalArgumentException("Volunteer quota must be greater than 0"))
        }

        val location = GeoPoint(
            latitude = latitude,
            longitude = longitude,
            address = trimmedAddress
        )

        return eventRepository.createEvent(
            title = trimmedTitle,
            description = trimmedDescription,
            categoryId = trimmedCategoryId,
            dateTimeMillis = dateTimeMillis,
            address = trimmedAddress,
            location = location,
            volunteerQuota = volunteerQuota,
            photoUrls = photoUrls
        )
    }
}
