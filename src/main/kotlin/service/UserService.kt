package com.example.service

import com.example.constants.SupportedTimezones
import com.example.exception.InvalidUserTimezoneException
import com.example.exception.UserNotFoundException
import com.example.repository.user.IUserRepository
import java.util.*

class UserService(private val userRepository: IUserRepository) {

    suspend fun getUser(id: UUID) = userRepository
        .findById(id)
        ?: throw UserNotFoundException()

    suspend fun setTimezone(userId: UUID, timezone: String) {
        if (timezone !in SupportedTimezones.all) {
            throw InvalidUserTimezoneException()
        }

        if (!userRepository.setTimezone(userId, timezone)) {
            throw UserNotFoundException()
        }
    }
}