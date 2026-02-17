package com.example.db

import com.example.domain.UserType
import com.example.utils.pgEnum
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.javatime.timestamp
import java.util.*

object UsersTable : UUIDTable("users") {
    val name = varchar("name", 50)
    val email = varchar("email", 120)
    val passwordHash = varchar("password_hash", 64)
    val userType = pgEnum<UserType>("type", "user_type")
    val isPremium = bool("is_premium")
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at").nullable()
}

class UserDao(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<UserDao>(UsersTable)

    var name by UsersTable.name
    var email by UsersTable.email
    var passwordHash by UsersTable.passwordHash
    var userType by UsersTable.userType
    var isPremium by UsersTable.isPremium
    var createdAt by UsersTable.createdAt
    var updatedAt by UsersTable.updatedAt
}