package com.arathort.localinteraction.domain.model

enum class EventStatus(val apiValue: String) {
    PENDING_MODERATION("Очікує модерації"),
    OPEN("Відкрито"),
    IN_PROGRESS("In Process"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled");

    companion object {
        fun fromApiValue(value: String): EventStatus =
            entries.find { it.apiValue.equals(value, ignoreCase = true) || it.name.equals(value, ignoreCase = true) }
                ?: OPEN
    }
}

enum class ModerationStatus(val apiValue: String) {
    PENDING("На розгляді"),
    APPROVED("Approved"),
    REJECTED("Rejected");

    companion object {
        fun fromApiValue(value: String): ModerationStatus =
            entries.find { it.apiValue.equals(value, ignoreCase = true) || it.name.equals(value, ignoreCase = true) }
                ?: PENDING
    }
}
