package com.arathort.localinteraction.data.mapper

import com.arathort.localinteraction.data.dto.EventDto
import com.arathort.localinteraction.domain.model.Event
import com.arathort.localinteraction.domain.model.EventStatus
import com.arathort.localinteraction.domain.model.GeoPoint

fun EventDto.toDomain(): Event = Event(
    id = id,
    title = title,
    description = description,
    categoryId = categoryId,
    categoryName = categoryName,
    status = runCatching { EventStatus.valueOf(status) }.getOrDefault(EventStatus.OPEN),
    dateTimeMillis = dateTimeMillis,
    address = address,
    location = GeoPoint(latitude = latitude, longitude = longitude),
    volunteerQuota = volunteerQuota,
    approvedCount = approvedCount,
    photoUrls = photoUrls,
    organizerId = organizerId,
    organizerName = organizerName,
    organizerAvatarUrl = organizerAvatarUrl,
    organizerRating = organizerRating,
    organizerContactPhone = organizerContactPhone,
    rejectionReason = rejectionReason
)

fun Event.toDto(): EventDto = EventDto(
    id = id,
    title = title,
    description = description,
    categoryId = categoryId,
    categoryName = categoryName,
    status = status.name,
    dateTimeMillis = dateTimeMillis,
    address = address,
    latitude = location.latitude,
    longitude = location.longitude,
    volunteerQuota = volunteerQuota,
    approvedCount = approvedCount,
    photoUrls = photoUrls,
    organizerId = organizerId,
    organizerName = organizerName,
    organizerAvatarUrl = organizerAvatarUrl,
    organizerRating = organizerRating,
    organizerContactPhone = organizerContactPhone,
    rejectionReason = rejectionReason
)
