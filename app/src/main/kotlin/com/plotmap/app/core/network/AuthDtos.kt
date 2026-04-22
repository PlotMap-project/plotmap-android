package com.plotmap.app.core.network
import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    val email: String,
    val password: String,
    val name: String,
)

@Serializable
data class LoginEmailRequest(
    val email: String,
    val password: String,
)

@Serializable
data class LoginNameRequest(
    val name: String,
    val password: String,
)

@Serializable
data class AuthResponse(
    val userId: String,
    val email: String,
    val name: String,
    val token: String,
)

@Serializable
data class ApiErrorResponse(
    val error: String,
    val message: String,
)
