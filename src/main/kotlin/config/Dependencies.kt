package com.example.config

import com.example.repository.foods.app.AppFoodRepository
import com.example.repository.foods.log.FoodLogRepository
import com.example.repository.foods.pending.PendingFoodRepository
import com.example.repository.nutrient.NutrientRepository
import com.example.repository.nutrient.intake.NutrientIntakeRepository
import com.example.repository.refresh.RefreshRepository
import com.example.repository.user.UserRepository
import com.example.repository.user.wallets.UserWalletRepository
import com.example.service.*
import com.example.utils.date_time.DateTimeParser
import com.example.utils.date_time.TimeConverter
import io.ktor.server.application.*

fun Application.configureDependencies() {
    // Instantiate utility classes
    val dateTimeParser = DateTimeParser()
    val timeConverter = TimeConverter(dateTimeParser)

    // Instantiate repositories
    val refreshRepository = RefreshRepository()
    val userRepository = UserRepository()
    val userWalletsRepository = UserWalletRepository()
    val nutrientRepository = NutrientRepository()
    val nutrientIntakeRepository = NutrientIntakeRepository()
    val appFoodRepository = AppFoodRepository()
    val pendingFoodRepository = PendingFoodRepository()
    val foodLogRepository = FoodLogRepository()

    // Instantiate services
    val jwtService = JwtService(this)
    val authService = AuthService(userRepository, refreshRepository, jwtService, userWalletsRepository)
    val userService = UserService(userRepository)
    val nutrientIntakeService = NutrientIntakeService(
        nutrientRepository,
        nutrientIntakeRepository,
        timeConverter
    )
    val appFoodService = AppFoodService(appFoodRepository)
    val pendingFoodService = PendingFoodService(
        pendingFoodRepository,
        userWalletsRepository,
        userRepository,
        appFoodRepository
    )
    val foodLogService = FoodLogService(
        foodLogRepository,
        nutrientIntakeRepository,
        timeConverter
    )

    // Register needed application attributes
    this.attributes.put(JwtServiceKey, jwtService)
    this.attributes.put(AuthServiceKey, authService)
    this.attributes.put(UserServiceKey, userService)
    this.attributes.put(NutrientIntakeServiceKey, nutrientIntakeService)
    this.attributes.put(AppFoodServiceKey, appFoodService)
    this.attributes.put(PendingFoodServiceKey, pendingFoodService)
    this.attributes.put(FoodLogServiceKey, foodLogService)
}