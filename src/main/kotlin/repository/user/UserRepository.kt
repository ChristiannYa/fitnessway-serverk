package com.example.repository.user

import com.example.domain.User
import com.example.domain.UserCreate
import com.example.mapping.U
import com.example.mapping.UDao
import com.example.mapping.toDomain
import com.example.utils.suspendTransaction
import java.time.Instant
import java.util.*

class UserRepository : IUserRepository {
    override suspend fun findById(id: UUID): User? = suspendTransaction {
        UDao.Companion
            .find { U.id eq id }
            .singleOrNull()
            ?.toDomain()
    }

    override suspend fun findByEmail(email: String): User? = suspendTransaction {
        UDao.Companion
            .find { U.email eq email }
            .singleOrNull()
            ?.toDomain()
    }

    override suspend fun create(user: UserCreate): User = suspendTransaction {
        UDao.Companion
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