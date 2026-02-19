package com.example.repository.refresh

import com.example.domain.RefreshToken
import com.example.domain.RefreshTokenCreate
import com.example.domain.TokenValidationResult
import com.example.mapping.RefreshTokenDao
import com.example.mapping.RefreshTokensTable
import com.example.mapping.UsersTable
import com.example.mapping.toDomain
import com.example.utils.suspendTransaction
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.SqlExpressionBuilder.less
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import java.time.Instant
import java.util.*

class RefreshRepository : IRefreshRepository {
    override suspend fun save(refreshTokenCreate: RefreshTokenCreate): RefreshToken = suspendTransaction {
        RefreshTokenDao.Companion
            .new {
                userId = EntityID(refreshTokenCreate.userId, UsersTable)
                deviceName = refreshTokenCreate.deviceName
                hash = refreshTokenCreate.hash
                expiresAt = refreshTokenCreate.expiresAt
                createdAt = Instant.now()
                lastUsedAt = null
                revokedAt = null
            }
            .toDomain()
    }

    override suspend fun validate(tokenHash: String, userId: UUID): TokenValidationResult = suspendTransaction {
        val token = RefreshTokenDao.Companion
            .find {
                (RefreshTokensTable.hash eq tokenHash) and
                        (RefreshTokensTable.userId eq userId)
            }
            .firstOrNull()
            ?.toDomain()

        when {
            token == null -> TokenValidationResult.NotFound
            token.isRevoked() -> TokenValidationResult.Revoked
            token.isExpired() -> TokenValidationResult.Expired
            else -> TokenValidationResult.Valid(token)
        }
    }

    override suspend fun findByHash(tokenHash: String): RefreshToken? = suspendTransaction {
        RefreshTokenDao.Companion
            .find { RefreshTokensTable.hash eq tokenHash }
            .firstOrNull()
            ?.toDomain()
    }

    override suspend fun revokeByHash(tokenHash: String): Boolean = suspendTransaction {
        RefreshTokenDao.Companion
            .find { RefreshTokensTable.hash eq tokenHash }
            .firstOrNull()?.let { token ->
                token.revokedAt = Instant.now()
                true
            } ?: false
    }

    override suspend fun revokeByUserId(userId: UUID): Int = suspendTransaction {
        val tokens = RefreshTokenDao.Companion
            .find {
                (RefreshTokensTable.userId eq userId) and
                        (RefreshTokensTable.revokedAt.isNull())
            }

        val now = Instant.now()
        tokens.forEach { it.revokedAt = now }

        tokens.count().toInt()
    }

    override suspend fun updateLastUsedTime(tokenHash: String): Unit = suspendTransaction {
        RefreshTokenDao.Companion
            .find { RefreshTokensTable.hash eq tokenHash }
            .firstOrNull()?.let { it.lastUsedAt = Instant.now() }
    }

    override suspend fun deleteExpired(): Int = suspendTransaction {
        RefreshTokensTable.deleteWhere {
            expiresAt less Instant.now()
        }
    }
}