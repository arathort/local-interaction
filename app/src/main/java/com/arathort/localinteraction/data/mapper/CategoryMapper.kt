package com.arathort.localinteraction.data.mapper

import com.arathort.localinteraction.data.dto.CategoryAdminResponseDto
import com.arathort.localinteraction.data.dto.CategoryDto
import com.arathort.localinteraction.domain.model.Category

fun CategoryDto.toDomain(): Category = Category(
    id = id,
    name = name,
    boundEventsCount = boundEventsCount,
    isArchived = isArchived
)

fun CategoryAdminResponseDto.toDomain(): Category = Category(
    id = id,
    name = name,
    boundEventsCount = activeEventsCount,
    totalEventsCount = totalEventsCount,
    isArchived = isArchived
)

fun Category.toDto(): CategoryDto = CategoryDto(
    id = id,
    name = name,
    boundEventsCount = boundEventsCount,
    isArchived = isArchived
)
