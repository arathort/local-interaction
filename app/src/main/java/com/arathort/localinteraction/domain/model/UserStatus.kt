package com.arathort.localinteraction.domain.model

enum class UserStatus(val apiValue: String) {
    ACTIVE("Активний"),
    BLOCKED("Заблокований");

    companion object {
        fun fromApiValue(value: String): UserStatus =
            entries.find { it.apiValue.equals(value, ignoreCase = true) || it.name.equals(value, ignoreCase = true) }
                ?: ACTIVE
    }
}
