package com.arathort.localinteraction.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponseDto(
    @SerialName("detail") val detail: String? = null,
    @SerialName("message") val message: String? = null,
    @SerialName("error") val error: String? = null,
)
