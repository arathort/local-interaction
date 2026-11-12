package com.arathort.localinteraction.domain.model

enum class EventStatus(val apiValue: String) {
    PENDING_MODERATION("Pending Moderation"),
    OPEN("Open"),
    IN_PROGRESS("In Process"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled");

    companion object {
        fun fromApiValue(value: String): EventStatus =
            when {
                value.equals("Pending Moderation", ignoreCase = true) ||
                    value.equals("PENDING_MODERATION", ignoreCase = true) ||
                    value.equals("Очікує модерації", ignoreCase = true) ||
                    value.equals("На розгляді", ignoreCase = true) -> PENDING_MODERATION
                value.equals("Open", ignoreCase = true) ||
                    value.equals("Відкрито", ignoreCase = true) -> OPEN
                value.equals("In Process", ignoreCase = true) ||
                    value.equals("IN_PROGRESS", ignoreCase = true) ||
                    value.equals("В процесі", ignoreCase = true) -> IN_PROGRESS
                value.equals("Completed", ignoreCase = true) ||
                    value.equals("Виконано", ignoreCase = true) -> COMPLETED
                value.equals("Cancelled", ignoreCase = true) ||
                    value.equals("Скасовано", ignoreCase = true) -> CANCELLED
                else -> OPEN
            }
    }
}

enum class ModerationStatus(val apiValue: String) {
    PENDING("Pending"),
    APPROVED("Approved"),
    REJECTED("Rejected");

    companion object {
        fun fromApiValue(value: String): ModerationStatus =
            when {
                value.equals("Pending", ignoreCase = true) ||
                    value.equals("На розгляді", ignoreCase = true) -> PENDING
                value.equals("Approved", ignoreCase = true) ||
                    value.equals("Схвалено", ignoreCase = true) -> APPROVED
                value.equals("Rejected", ignoreCase = true) ||
                    value.equals("Відхилено", ignoreCase = true) -> REJECTED
                else -> PENDING
            }
    }
}
