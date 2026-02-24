package com.example.mapping

import com.example.domain.RefreshToken
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.javatime.timestamp
import java.util.*

object RT : UUIDTable("refresh_tokens") {
    val userId = reference("user_id", U)
    val deviceName = varchar("device_name", 255)
    val hash = varchar("hash", 255)
    val createdAt = timestamp("created_at")
    val expiresAt = timestamp("expires_at")
    val lastUsedAt = timestamp("last_used_at").nullable()
    val revokedAt = timestamp("revoked_at").nullable()
}

class RTDao(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<RTDao>(RT)

    var userId by RT.userId
    var deviceName by RT.deviceName
    var hash by RT.hash
    var createdAt by RT.createdAt
    var expiresAt by RT.expiresAt
    var lastUsedAt by RT.lastUsedAt
    var revokedAt by RT.revokedAt
}

fun RTDao.toDomain() = RefreshToken(
    this.id.value,
    this.userId.value,
    this.hash,
    this.expiresAt,
    this.createdAt,
    this.lastUsedAt,
    this.revokedAt
)