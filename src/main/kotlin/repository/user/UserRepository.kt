package com.example.repository.user

import com.example.domain.User
import com.example.domain.UserCreate
import com.example.mapping.UserDao
import com.example.mapping.UsersTable
import com.example.mapping.toDomain
import com.example.utils.suspendTransaction
import java.time.Instant
import java.util.*

class UserRepository : IUserRepository {
    override suspend fun findById(id: UUID): User? = suspendTransaction {
        UserDao.Companion
            .find { UsersTable.id eq id }
            .singleOrNull()
            ?.toDomain()
    }

    override suspend fun findByEmail(email: String): User? = suspendTransaction {
        UserDao.Companion
            .find { UsersTable.email eq email }
            .singleOrNull()
            ?.toDomain()
    }

    override suspend fun create(user: UserCreate): User = suspendTransaction {
        UserDao.Companion
            .new {
                name = user.name
                email = user.email
                passwordHash = user.passwordHash
                userType = user.userType
                isPremium = false
                createdAt = Instant.now()
                updatedAt = null
            }
            .toDomain()
    }
}