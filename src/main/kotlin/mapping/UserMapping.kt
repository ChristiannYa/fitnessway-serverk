package com.example.mapping

import com.example.domain.User
import com.example.domain.UserType
import com.example.utils.pgEnum
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.javatime.timestamp
import java.util.*
import kotlin.time.toKotlinInstant

object U : UUIDTable("users") {
    val name = varchar("name", 50)
    val email = varchar("email", 120)
    val passwordHash = varchar("password_hash", 64)
    val userType = pgEnum<UserType>("type", "user_type")
    val isPremium = bool("is_premium")
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at").nullable()
}

class UDao(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<UDao>(U)

    var name by U.name
    var email by U.email
    var passwordHash by U.passwordHash
    var userType by U.userType
    var isPremium by U.isPremium
    var createdAt by U.createdAt
    var updatedAt by U.updatedAt
}

fun UDao.toDto() = User(
    this.id.value,
    this.name,
    this.email,
    this.passwordHash,
    this.isPremium,
    this.createdAt.toKotlinInstant(),
    this.updatedAt?.toKotlinInstant(),
    this.userType
)