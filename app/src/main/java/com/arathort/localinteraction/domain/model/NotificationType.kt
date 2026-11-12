package com.arathort.localinteraction.domain.model

enum class NotificationType(val apiValue: String) {
    EVENT_MODERATION("Event moderation"),
    NEW_APPLICATION("New application"),
    APPLICATION_STATUS("Application status"),
    APPLICATION_WITHDRAWAL("Application withdrawal"),
    EVENT_STATUS_CHANGE("Event status change"),
    NEW_REVIEW("New review"),
    NEW_COMMENT("New comment"),
    ACCOUNT_SECURITY("Account security");

    companion object {
        fun fromApiValue(value: String): NotificationType =
            entries.firstOrNull { it.apiValue.equals(value, ignoreCase = true) }
                ?: ACCOUNT_SECURITY
    }
}
