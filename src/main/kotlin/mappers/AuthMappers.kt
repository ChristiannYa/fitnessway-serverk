package com.example.mappers

import com.example.domain.AccessTokenClaims
import com.example.domain.RefreshTokenClaims

fun AccessTokenClaims.toMap() = mapOf(
    "sessionId" to this.sessionId,
    "userId" to this.userPrincipal.id.toString(),
    "type" to this.userPrincipal.type.name,
    "isPremium" to this.userPrincipal.isPremium,
    "timezone" to this.userPrincipal.timezone
)

fun RefreshTokenClaims.toMap() = mapOf(
    "userId" to this.userId
)