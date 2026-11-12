package com.arathort.localinteraction.domain.model

enum class UserRole(val apiValue: String) {
    GUEST("Guest"),
    VOLUNTEER("Volunteer"),
    ORGANIZER("Organizer"),
    ADMIN("Admin");

    companion object {
        fun fromApiValue(value: String): UserRole =
            when {
                value.equals("Volunteer", ignoreCase = true) ||
                    value.equals("Волонтер", ignoreCase = true) -> VOLUNTEER
                value.equals("Organizer", ignoreCase = true) ||
                    value.equals("Організатор", ignoreCase = true) -> ORGANIZER
                value.equals("Admin", ignoreCase = true) ||
                    value.equals("Адміністратор", ignoreCase = true) -> ADMIN
                else -> GUEST
            }
    }
}
