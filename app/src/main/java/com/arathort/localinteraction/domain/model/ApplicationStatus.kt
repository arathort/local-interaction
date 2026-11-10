package com.arathort.localinteraction.domain.model

enum class ApplicationStatus(val apiValue: String) {
    PENDING("Pending"),
    APPROVED("Approved"),
    REJECTED("Rejected"),
    WITHDRAWN("Withdrawn");

    companion object {
        fun fromApiValue(value: String): ApplicationStatus =
            when {
                value.equals("Pending", ignoreCase = true) ||
                    value.equals("Очікує розгляду", ignoreCase = true) -> PENDING
                value.equals("Approved", ignoreCase = true) ||
                    value.equals("Схвалено", ignoreCase = true) -> APPROVED
                value.equals("Rejected", ignoreCase = true) ||
                    value.equals("Відхилено", ignoreCase = true) -> REJECTED
                value.equals("Withdrawn", ignoreCase = true) ||
                    value.equals("Відкликано", ignoreCase = true) -> WITHDRAWN
                else -> PENDING
            }
    }
}
