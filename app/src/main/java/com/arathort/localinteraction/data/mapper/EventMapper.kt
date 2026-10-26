package com.arathort.localinteraction.data.mapper

import com.arathort.localinteraction.data.dto.EventDto
import com.arathort.localinteraction.data.dto.GeoLocationDto
import com.arathort.localinteraction.data.dto.OrganizerShortInfoDto
import com.arathort.localinteraction.domain.model.Event
import com.arathort.localinteraction.domain.model.EventStatus
import com.arathort.localinteraction.domain.model.GeoPoint
import com.arathort.localinteraction.domain.model.ModerationStatus
import com.arathort.localinteraction.domain.model.OrganizerShortInfo

fun GeoLocationDto.toDomain(): GeoPoint = GeoPoint(
    latitude = latitude,
    longitude = longitude,
    address = address
)

fun GeoPoint.toDto(addressOverride: String = address): GeoLocationDto = GeoLocationDto(
    address = addressOverride,
    latitude = latitude,
    longitude = longitude
)

fun OrganizerShortInfoDto.toDomain(): OrganizerShortInfo = OrganizerShortInfo(
    id = id,
    fullName = fullName,
    avatarUrl = avatarUrl,
    rating = rating
)

fun OrganizerShortInfo.toDto(): OrganizerShortInfoDto = OrganizerShortInfoDto(
    id = id,
    fullName = fullName,
    avatarUrl = avatarUrl,
    rating = rating
)

fun EventDto.toDomain(): Event = Event(
    id = id,
    title = title,
    description = description,
    categoryId = categoryId,
    categoryName = categoryName,
    status = EventStatus.fromApiValue(status),
    moderationStatus = ModerationStatus.fromApiValue(moderationStatus),
    eventDate = eventDate,
    location = location.toDomain(),
    volunteerQuota = volunteerQuota,
    approvedVolunteersCount = approvedVolunteersCount,
    progressText = progressText,
    photos = photos,
    organizer = organizer.toDomain(),
    rejectionReason = rejectionReason,
    createdAt = createdAt
)

fun Event.toDto(): EventDto = EventDto(
    id = id,
    title = title,
    description = description,
    categoryId = categoryId,
    categoryName = categoryName,
    status = status.apiValue,
    moderationStatus = moderationStatus.apiValue,
    eventDate = eventDate,
    location = location.toDto(),
    volunteerQuota = volunteerQuota,
    approvedVolunteersCount = approvedVolunteersCount,
    progressText = progressText,
    photos = photos,
    organizer = organizer.toDto(),
    rejectionReason = rejectionReason,
    createdAt = createdAt
)
