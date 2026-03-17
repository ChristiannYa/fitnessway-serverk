package com.example.domain

import com.example.exception.InvalidTokenException
import java.time.Instant
import java.util.*

enum class TokenType {
    ACCESS,
    REFRESH
}

data class RefreshToken(
    val id: UUID,
    val userId: UUID,
    val hash: String,
    val expiresAt: Instant,
    val createdAt: Instant,
    val lastUsedAt: Instant? = null,
    val revokedAt: Instant? = null
) {
    fun isExpired(): Boolean = expiresAt.isBefore(Instant.now())
    fun isRevoked(): Boolean = revokedAt != null
}

sealed class TokenValidationResult {
    data class Valid(val token: RefreshToken) : TokenValidationResult()
    object NotFound : TokenValidationResult()
    object Revoked : TokenValidationResult()
    object Expired : TokenValidationResult()
}

fun TokenValidationResult.getTokenOrThrow(): RefreshToken = when (this) {
    is TokenValidationResult.NotFound ->
        throw InvalidTokenException("Refresh Token not found")

    is TokenValidationResult.Revoked ->
        throw InvalidTokenException("Refresh Token revoked")

    is TokenValidationResult.Expired ->
        throw InvalidTokenException("Refresh token expired")

    is TokenValidationResult.Valid -> this.token
}

data class RefreshTokenCreate(
    val hash: String,
    val userId: UUID,
    val expiresAt: Instant,
    val deviceName: String
)

data class AccessTokenClaims(
    val sessionId: String,
    val userPrincipal: UserPrincipal
)

fun AccessTokenClaims.toMap() = mapOf(
    "sessionId" to this.sessionId,
    "userId" to this.userPrincipal.id.toString(),
    "type" to this.userPrincipal.type.name,
    "isPremium" to this.userPrincipal.isPremium
)

data class RefreshTokenClaims(
    val userId: String
)

fun RefreshTokenClaims.toMap() = mapOf(
    "userId" to this.userId
)

data class TokenStrings(
    val accessToken: String,
    val refreshToken: String
)

data class UserLoginData(
    val email: String,
    val password: String,
)

data class UserRegisterData(
    val name: String,
    val email: String,
    val password: String,
)