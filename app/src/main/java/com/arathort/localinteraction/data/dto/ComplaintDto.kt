package com.arathort.localinteraction.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ComplaintCreateDto(
    @SerialName("target_type") val targetType: String,
    @SerialName("target_id") val targetId: String,
    @SerialName("violation_category") val violationCategory: String,
    @SerialName("description") val description: String
)

@Serializable
data class ComplaintDto(
    @SerialName("id") val id: String,
    @SerialName("reporter_id") val reporterId: String,
    @SerialName("reporter_name") val reporterName: String,
    @SerialName("violator_id") val violatorId: String = "",
    @SerialName("violator_name") val violatorName: String = "",
    @SerialName("reason") val reason: String = "",
    @SerialName("created_at_millis") val createdAtMillis: Long = 0L,
    @SerialName("is_resolved") val isResolved: Boolean = false
)
