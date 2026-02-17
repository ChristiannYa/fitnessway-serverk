package com.example.mappers

import com.example.db.RefreshTokenDao
import com.example.domain.RefreshToken

fun RefreshTokenDao.toDomain() = RefreshToken(
    this.id.value,
    this.userId,
    this.hash,
    this.expiresAt,
    this.createdAt,
    this.lastUsedAt,
    this.revokedAt
)