package com.arathort.localinteraction.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ComplaintDto(
    @SerialName("id") val id: String,
    @SerialName("reporter_id") val reporterId: String,
    @SerialName("reporter_name") val reporterName: String,
    @SerialName("violator_id") val violatorId: String,
    @SerialName("violator_name") val violatorName: String,
    @SerialName("reason") val reason: String,
    @SerialName("created_at_millis") val createdAtMillis: Long,
    @SerialName("is_resolved") val isResolved: Boolean = false
)
