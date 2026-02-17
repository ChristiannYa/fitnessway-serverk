package com.example.repository.user

import com.example.domain.User
import com.example.domain.UserCreate
import java.util.*

interface IUserRepository {
    suspend fun findById(id: UUID): User?
    suspend fun findByEmail(email: String): User?
    suspend fun create(user: UserCreate): User
}