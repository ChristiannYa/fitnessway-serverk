package com.example.repository.user

import com.example.domain.User
import com.example.domain.UserCreate
import com.example.domain.UserType
import com.example.mapping.U
import com.example.mapping.UDao
import com.example.mapping.toDto
import com.example.utils.suspendTransaction
import org.jetbrains.exposed.sql.update
import java.time.Instant
import java.util.*

class UserRepository : IUserRepository {
    override suspend fun findById(id: UUID): User? = suspendTransaction {
        UDao.Companion
            .find { U.id eq id }
            .singleOrNull()
            ?.toDto()
    }

    override suspend fun findByEmail(email: String): User? = suspendTransaction {
        UDao.Companion
            .find { U.email eq email }
            .singleOrNull()
            ?.toDto()
    }

    override suspend fun setTimezone(
        userId: UUID,
        timezone: String
    ): Boolean = suspendTransaction {
        val updateCount = U.update(
            where = { U.id eq userId }
        ) {
            it[U.timezone] = timezone
        }

        updateCount == 1
    }

    override suspend fun create(user: UserCreate): User = suspendTransaction {
        UDao.Companion
            .new {
                name = user.name
                email = user.email
                passwordHash = user.passwordHash
                userType = UserType.USER
                isPremium = false
                createdAt = Instant.now()
                updatedAt = null
            }
            .toDto()
    }
}