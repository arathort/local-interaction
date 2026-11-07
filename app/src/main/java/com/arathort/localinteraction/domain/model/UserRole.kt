package com.arathort.localinteraction.domain.model

enum class UserRole(val apiValue: String) {
    GUEST("Guest"),
    VOLUNTEER("Volunteer"),
    ORGANIZER("Organizer"),
    ADMIN("Admin");

    companion object {
        fun fromApiValue(value: String): UserRole =
            entries.find { it.apiValue.equals(value, ignoreCase = true) || it.name.equals(value, ignoreCase = true) }
                ?: GUEST
    }
}
