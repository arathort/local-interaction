package com.arathort.localinteraction.domain.model

data class Complaint(
    val id: String,
    val reporterId: String,
    val reporterName: String,
    val violatorId: String,
    val violatorName: String,
    val reason: String,
    val createdAtMillis: Long,
    val isResolved: Boolean = false
)
