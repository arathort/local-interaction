package com.arathort.localinteraction.domain.model

enum class UserStatus(val apiValue: String) {
    ACTIVE("Active"),
    BLOCKED("Blocked");

    companion object {
        fun fromApiValue(value: String): UserStatus =
            when {
                value.equals("Active", ignoreCase = true) ||
                    value.equals("Активний", ignoreCase = true) -> ACTIVE
                value.equals("Blocked", ignoreCase = true) ||
                    value.equals("Заблокований", ignoreCase = true) -> BLOCKED
                else -> ACTIVE
            }
    }
}
