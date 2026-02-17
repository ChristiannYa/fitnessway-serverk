package com.example.config

import com.example.repository.refresh.RefreshRepository
import com.example.repository.user.UserRepository
import com.example.service.AuthService
import com.example.service.JwtService
import io.ktor.server.application.*

fun Application.configureDependencies() {
    // Instantiate repositories
    val userRepository = UserRepository()
    val refreshRepository = RefreshRepository()

    // Instantiate services
    val jwtService = JwtService(this)
    val authService = AuthService(userRepository, refreshRepository, jwtService)

    // Register application attributes
    this.attributes.put(UserRepositoryKey, userRepository)
    this.attributes.put(RefreshRepositoryKey, refreshRepository)
    this.attributes.put(JwtServiceKey, jwtService)
    this.attributes.put(AuthServiceKey, authService)
}