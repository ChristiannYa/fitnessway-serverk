package com.example.service

import com.example.exception.UserNotFoundException
import com.example.repository.user.IUserRepository
import java.util.*

class UserService(private val userRepository: IUserRepository) {
    suspend fun getUser(id: UUID) = userRepository.findById(id)
        ?: throw UserNotFoundException()
}