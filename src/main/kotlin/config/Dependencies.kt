package com.example.config

import com.example.repository.foods.app.AppFoodRepository
import com.example.repository.foods.pending.PendingFoodRepository
import com.example.repository.refresh.RefreshRepository
import com.example.repository.user.UserRepository
import com.example.repository.user.wallets.UserWalletRepository
import com.example.service.AuthService
import com.example.service.JwtService
import com.example.service.PendingFoodService
import com.example.service.UserService
import io.ktor.server.application.*

fun Application.configureDependencies() {
    // Instantiate repositories
    val userRepository = UserRepository()
    val refreshRepository = RefreshRepository()
    val pendingFoodRepository = PendingFoodRepository()
    val userWalletsRepository = UserWalletRepository()
    val appFoodRepository = AppFoodRepository()

    // Instantiate services
    val jwtService = JwtService(this)
    val authService = AuthService(userRepository, refreshRepository, jwtService, userWalletsRepository)
    val pendingFoodService = PendingFoodService(
        pendingFoodRepository,
        userWalletsRepository,
        userRepository,
        appFoodRepository
    )
    val userService = UserService(userRepository)

    // Register needed application attributes
    this.attributes.put(JwtServiceKey, jwtService)
    this.attributes.put(AuthServiceKey, authService)
    this.attributes.put(PendingFoodServiceKey, pendingFoodService)
    this.attributes.put(UserServiceKey, userService)
}