package com.arathort.localinteraction.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SupportTicketCreateDto(
    @SerialName("subject") val subject: String,
    @SerialName("message") val message: String,
    @SerialName("contact_email") val contactEmail: String? = null
)

@Serializable
data class SupportTicketResponseDto(
    @SerialName("id") val id: String,
    @SerialName("user_id") val userId: String? = null,
    @SerialName("user_name") val userName: String? = null,
    @SerialName("contact_email") val contactEmail: String,
    @SerialName("subject") val subject: String,
    @SerialName("message") val message: String,
    @SerialName("status") val status: String,
    @SerialName("admin_reply") val adminReply: String? = null,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String
)

@Serializable
data class SupportTicketUpdateStatusDto(
    @SerialName("status") val status: String,
    @SerialName("admin_reply") val adminReply: String? = null
)
