package com.example.mappers

import com.example.db.UserDao
import com.example.domain.User
import com.example.domain.UserPrincipal
import com.example.dto.UserDto

fun User.toPrincipal() = UserPrincipal(
    this.id,
    this.type,
    this.isPremium
)

fun User.toDto() = UserDto(
    this.id.toString(),
    this.name,
    this.email,
    this.isPremium.toString(),
    this.createdAt.toString(),
    this.updatedAt.toString(),
    this.type.toString()
)

fun UserDao.toDomain() = User(
    this.id.value,
    this.name,
    this.email,
    this.passwordHash,
    this.isPremium,
    this.createdAt,
    this.updatedAt,
    this.userType
)