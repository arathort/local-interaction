package com.arathort.localinteraction.domain.model

enum class ComplaintTargetType(val apiValue: String) {
    USER("User"),
    EVENT("Event"),
    COMMENT("Comment");

    companion object {
        fun fromApiValue(value: String): ComplaintTargetType =
            entries.firstOrNull { it.apiValue.equals(value, ignoreCase = true) } ?: USER
    }
}
