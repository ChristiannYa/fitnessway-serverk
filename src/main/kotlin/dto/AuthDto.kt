package com.example.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginRequestDto(
    val email: String,
    val password: String,

    @SerialName("device_name")
    val deviceName: String
)

@Serializable
data class RegisterRequestDto(
    val name: String,
    val email: String,
    val password: String,

    @SerialName("device_name")
    val deviceName: String,

    @SerialName("confirmed_password")
    val confirmedPassword: String
)

@Serializable
data class RefreshRequestDto(
    @SerialName("refresh_token")
    val refreshToken: String
)

@Serializable
data class LogoutRequestDto(
    @SerialName("refresh_token")
    val refreshToken: String
)