package com.example.config

import com.example.repository.edible.app.AppFoodRepository
import com.example.repository.edible.log.FoodLogRepository
import com.example.repository.edible.pending.PendingFoodRepository
import com.example.repository.edible.user.UserEdibleRepository
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
    val userEdibleRepository = UserEdibleRepository()
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
    val userEdibleService = UserEdibleService(userEdibleRepository)
    val pendingFoodService = PendingFoodService(
        pendingFoodRepository,
        userWalletsRepository,
        userRepository,
        appFoodRepository
    )
    val edibleLogService = EdibleLogService(
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
    this.attributes.put(UserEdibleServiceKey, userEdibleService)
    this.attributes.put(PendingFoodServiceKey, pendingFoodService)
    this.attributes.put(EdibleLogServiceKey, edibleLogService)
}