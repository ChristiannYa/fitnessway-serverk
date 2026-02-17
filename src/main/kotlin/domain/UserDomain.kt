package com.example.domain

import java.time.Instant
import java.util.*

enum class UserType {
    USER, CONTRIBUTOR, ADMIN
}

data class User(
    val id: UUID,
    val name: String,
    val email: String,
    val passwordHash: String,
    val isPremium: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant? = null,
    val type: UserType,
)

data class UserPrincipal(
    val id: UUID,
    val type: UserType,
    val isPremium: Boolean
)

data class UserCreate(
    val name: String,
    val email: String,
    val passwordHash: String
)