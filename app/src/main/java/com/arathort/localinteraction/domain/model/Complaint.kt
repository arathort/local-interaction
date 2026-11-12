package com.arathort.localinteraction.domain.model

data class Complaint(
    val id: String,
    val reporterId: String,
    val reporterName: String,
    val violatorId: String,
    val violatorName: String,
    val reason: String,
    val createdAtMillis: Long = 0L,
    val isResolved: Boolean = false,
    val targetType: String = "User",
    val targetId: String = "",
    val targetSummary: String = "",
    val violationCategory: String = "",
    val status: String = if (isResolved) "Resolved" else "Pending",
    val adminNote: String? = null,
    val createdAt: String = ""
)
