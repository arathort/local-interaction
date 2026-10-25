package com.arathort.localinteraction.data.mapper

import com.arathort.localinteraction.data.dto.UserDto
import com.arathort.localinteraction.domain.model.User
import com.arathort.localinteraction.domain.model.UserRole

fun UserDto.toDomain(): User = User(
    id = id,
    fullName = fullName,
    email = email,
    phone = phone,
    bio = bio,
    avatarUrl = avatarUrl,
    role = runCatching { UserRole.valueOf(role) }.getOrDefault(UserRole.GUEST),
    averageRating = averageRating,
    reviewsCount = reviewsCount,
    isBlocked = isBlocked,
    blockReason = blockReason
)

fun User.toDto(): UserDto = UserDto(
    id = id,
    fullName = fullName,
    email = email,
    phone = phone,
    bio = bio,
    avatarUrl = avatarUrl,
    role = role.name,
    averageRating = averageRating,
    reviewsCount = reviewsCount,
    isBlocked = isBlocked,
    blockReason = blockReason
)
