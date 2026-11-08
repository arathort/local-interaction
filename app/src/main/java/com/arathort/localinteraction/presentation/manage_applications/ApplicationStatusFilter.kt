package com.arathort.localinteraction.presentation.manage_applications

import com.arathort.localinteraction.domain.model.ApplicationStatus

enum class ApplicationStatusFilter {
    ALL,
    PENDING,
    APPROVED,
    REJECTED,
    WITHDRAWN;

    fun matches(status: ApplicationStatus): Boolean = when (this) {
        ALL -> true
        PENDING -> status == ApplicationStatus.PENDING
        APPROVED -> status == ApplicationStatus.APPROVED
        REJECTED -> status == ApplicationStatus.REJECTED
        WITHDRAWN -> status == ApplicationStatus.WITHDRAWN
    }
}
