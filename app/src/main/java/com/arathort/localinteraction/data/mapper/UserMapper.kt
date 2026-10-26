package com.arathort.localinteraction.data.mapper

import com.arathort.localinteraction.data.dto.MyTaskHistoryItemDto
import com.arathort.localinteraction.data.dto.UserDto
import com.arathort.localinteraction.domain.model.ApplicationStatus
import com.arathort.localinteraction.domain.model.EventStatus
import com.arathort.localinteraction.domain.model.MyTaskHistoryItem
import com.arathort.localinteraction.domain.model.User
import com.arathort.localinteraction.domain.model.UserRole
import com.arathort.localinteraction.domain.model.UserStatus

fun MyTaskHistoryItemDto.toDomain(): MyTaskHistoryItem = MyTaskHistoryItem(
    applicationId = applicationId,
    eventId = eventId,
    eventTitle = eventTitle,
    eventStatus = EventStatus.fromApiValue(eventStatus),
    applicationStatus = ApplicationStatus.fromApiValue(applicationStatus),
    canWithdraw = canWithdraw,
    appliedAt = appliedAt
)

fun MyTaskHistoryItem.toDto(): MyTaskHistoryItemDto = MyTaskHistoryItemDto(
    applicationId = applicationId,
    eventId = eventId,
    eventTitle = eventTitle,
    eventStatus = eventStatus.apiValue,
    applicationStatus = applicationStatus.apiValue,
    canWithdraw = canWithdraw,
    appliedAt = appliedAt
)

fun UserDto.toDomain(): User = User(
    id = id,
    role = UserRole.fromApiValue(role),
    fullName = fullName,
    avatarUrl = avatarUrl,
    description = description,
    rating = rating,
    reviews = reviews.map { it.toDomain() },
    email = email,
    phone = phone,
    status = UserStatus.fromApiValue(status),
    myTasks = myTasks.map { it.toDomain() },
    blockReason = blockReason
)

fun User.toDto(): UserDto = UserDto(
    id = id,
    role = role.apiValue,
    fullName = fullName,
    avatarUrl = avatarUrl,
    description = description,
    rating = rating,
    reviews = reviews.map { it.toDto() },
    email = email,
    phone = phone,
    status = status.apiValue,
    myTasks = myTasks.map { it.toDto() },
    blockReason = blockReason
)
