package com.arathort.localinteraction.domain.model

enum class ApplicationStatus(val apiValue: String) {
    PENDING("Очікує розгляду"),
    APPROVED("Approved"),
    REJECTED("Rejected"),
    WITHDRAWN("Відкликано");

    companion object {
        fun fromApiValue(value: String): ApplicationStatus =
            entries.find { it.apiValue.equals(value, ignoreCase = true) || it.name.equals(value, ignoreCase = true) }
                ?: PENDING
    }
}
