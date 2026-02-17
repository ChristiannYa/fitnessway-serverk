package com.example.db

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.javatime.timestamp
import java.util.*

object RefreshTokensTable : UUIDTable("refresh_tokens") {
    val userId = uuid("user_id")
    val deviceName = varchar("device_name", 255)
    val hash = varchar("hash", 255)
    val createdAt = timestamp("created_at")
    val expiresAt = timestamp("expires_at")
    val lastUsedAt = timestamp("last_used_at").nullable()
    val revokedAt = timestamp("revoked_at").nullable()
}

class RefreshTokenDao(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<RefreshTokenDao>(RefreshTokensTable)

    var userId by RefreshTokensTable.userId
    var deviceName by RefreshTokensTable.deviceName
    var hash by RefreshTokensTable.hash
    var createdAt by RefreshTokensTable.createdAt
    var expiresAt by RefreshTokensTable.expiresAt
    var lastUsedAt by RefreshTokensTable.lastUsedAt
    var revokedAt by RefreshTokensTable.revokedAt
}