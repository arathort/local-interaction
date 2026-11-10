package com.arathort.localinteraction.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AuthTokenDto(
    @SerialName("access_token") val accessToken: String,
    @SerialName("token_type") val tokenType: String = "bearer"
)

@Serializable
data class OAuthLoginRequestDto(
    @SerialName("token") val token: String,
    @SerialName("role") val role: String? = "Volunteer",
    @SerialName("agreed_to_rules") val agreedToRules: Boolean = true
)

@Serializable
data class ForgotPasswordRequestDto(
    @SerialName("email") val email: String
)

@Serializable
data class ResetPasswordConfirmRequestDto(
    @SerialName("reset_token") val resetToken: String,
    @SerialName("new_password") val newPassword: String,
    @SerialName("new_password_confirm") val newPasswordConfirm: String
)
