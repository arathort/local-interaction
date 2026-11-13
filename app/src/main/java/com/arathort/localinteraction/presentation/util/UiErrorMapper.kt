package com.arathort.localinteraction.presentation.util

import androidx.annotation.StringRes
import com.arathort.localinteraction.R
import com.arathort.localinteraction.data.dto.ErrorResponseDto
import kotlinx.serialization.json.Json
import retrofit2.HttpException
import java.io.IOException

data class UiError(
    @param:StringRes val messageResId: Int,
    val customMessage: String? = null,
)

object UiErrorMapper {

    private val json = Json { ignoreUnknownKeys = true }

    fun map(throwable: Throwable): UiError {
        return when (throwable) {
            is IOException -> UiError(messageResId = R.string.error_network)
            is HttpException -> mapHttpException(throwable)
            else -> mapGenericThrowable(throwable)
        }
    }

    private fun mapHttpException(exception: HttpException): UiError {
        val rawBody = runCatching { exception.response()?.errorBody()?.string() }.getOrNull().orEmpty()
        val serverDetail = parseDetailFromRawJson(rawBody)
        val lowerDetail = serverDetail?.lowercase().orEmpty()

        when {
            lowerDetail.contains("already registered") || lowerDetail.contains("already exists") || lowerDetail.contains("вже існує") || lowerDetail.contains("зареєстрован") -> {
                return UiError(messageResId = R.string.error_email_already_exists)
            }
            lowerDetail.contains("invalid credentials") || lowerDetail.contains("incorrect password") || lowerDetail.contains("wrong password") || lowerDetail.contains("невірний") -> {
                return UiError(messageResId = R.string.error_auth_invalid_credentials)
            }
            lowerDetail.contains("user not found") || lowerDetail.contains("не знайдено") -> {
                return UiError(messageResId = R.string.error_user_not_found)
            }
            lowerDetail.contains("blocked") || lowerDetail.contains("suspended") || lowerDetail.contains("заблокован") -> {
                return UiError(messageResId = R.string.error_account_blocked)
            }
            lowerDetail.contains("invalid token") || lowerDetail.contains("expired token") || lowerDetail.contains("токен") -> {
                return UiError(messageResId = R.string.error_invalid_or_expired_token)
            }
        }

        if (!serverDetail.isNullOrBlank() && isCleanUserMessage(serverDetail)) {
            return UiError(messageResId = R.string.error_unknown, customMessage = serverDetail)
        }

        val resId = when (exception.code()) {
            400 -> R.string.error_auth_invalid_credentials
            401 -> R.string.error_auth_invalid_credentials
            403 -> R.string.error_account_blocked
            404 -> R.string.error_user_not_found
            409 -> R.string.error_email_already_exists
            429 -> R.string.error_too_many_requests
            in 500..599 -> R.string.error_server_unavailable
            else -> R.string.error_unknown
        }

        return UiError(messageResId = resId)
    }

    private fun mapGenericThrowable(throwable: Throwable): UiError {
        val message = throwable.message.orEmpty()
        val lowerMessage = message.lowercase()

        if (lowerMessage.contains("connect") || lowerMessage.contains("host") || lowerMessage.contains("timeout") || lowerMessage.contains("network")) {
            return UiError(messageResId = R.string.error_network)
        }

        if (message.isNotBlank() && isCleanUserMessage(message)) {
            return UiError(messageResId = R.string.error_unknown, customMessage = message)
        }

        return UiError(messageResId = R.string.error_unknown)
    }

    private fun parseDetailFromRawJson(rawJson: String): String? {
        if (rawJson.isBlank()) return null
        return runCatching {
            val dto = json.decodeFromString<ErrorResponseDto>(rawJson)
            dto.detail ?: dto.message ?: dto.error
        }.getOrNull()
    }

    private fun isCleanUserMessage(message: String): Boolean {
        val lower = message.lowercase()
        return !lower.startsWith("http ") &&
                !lower.contains("exception") &&
                !lower.contains("failed to connect") &&
                !lower.contains("retrofit")
    }
}
