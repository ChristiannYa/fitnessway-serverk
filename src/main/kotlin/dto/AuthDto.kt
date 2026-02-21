package com.example.dto

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val email: String,
    val password: String,
    val deviceName: String
)

@Serializable
data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    val confirmedPassword: String,
    val userType: String,
    val deviceName: String
)

@Serializable
data class RefreshRequest(
    val refreshToken: String
)

@Serializable
data class LogoutRequest(
    val refreshToken: String
)