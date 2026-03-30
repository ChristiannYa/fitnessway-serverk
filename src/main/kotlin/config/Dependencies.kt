package com.example.config

import com.example.repository.foods.app.AppFoodRepository
import com.example.repository.foods.log.FoodLogRepository
import com.example.repository.foods.pending.PendingFoodRepository
import com.example.repository.refresh.RefreshRepository
import com.example.repository.user.UserRepository
import com.example.repository.user.wallets.UserWalletRepository
import com.example.service.*
import io.ktor.server.application.*

fun Application.configureDependencies() {
    // Instantiate repositories
    val refreshRepository = RefreshRepository()
    val userRepository = UserRepository()
    val userWalletsRepository = UserWalletRepository()
    val appFoodRepository = AppFoodRepository()
    val pendingFoodRepository = PendingFoodRepository()
    val foodLogRepository = FoodLogRepository()

    // Instantiate services
    val jwtService = JwtService(this)
    val authService = AuthService(userRepository, refreshRepository, jwtService, userWalletsRepository)
    val userService = UserService(userRepository)
    val appFoodService = AppFoodService(
        appFoodRepository
    )
    val pendingFoodService = PendingFoodService(
        pendingFoodRepository,
        userWalletsRepository,
        userRepository,
        appFoodRepository
    )
    val foodLogService = FoodLogService(foodLogRepository)

    // Register needed application attributes
    this.attributes.put(JwtServiceKey, jwtService)
    this.attributes.put(AuthServiceKey, authService)
    this.attributes.put(UserServiceKey, userService)
    this.attributes.put(AppFoodServiceKey, appFoodService)
    this.attributes.put(PendingFoodServiceKey, pendingFoodService)
    this.attributes.put(FoodLogServiceKey, foodLogService)
}