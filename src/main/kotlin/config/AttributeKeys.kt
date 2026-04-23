package com.example.config

import com.example.domain.UserPrincipal
import com.example.service.*
import io.ktor.util.*

val UserPrincipalKey = AttributeKey<UserPrincipal>("UserPrincipal")
val JwtServiceKey = AttributeKey<JwtService>("JwtService")
val AuthServiceKey = AttributeKey<AuthService>("AuthService")
val NutrientIntakeServiceKey = AttributeKey<NutrientIntakeService>("NutrientIntakeServiceKey")
val AppFoodServiceKey = AttributeKey<AppFoodService>("AppFoodService")
val EdibleLogServiceKey = AttributeKey<EdibleLogService>("FoodLogServiceKey")
val PendingFoodServiceKey = AttributeKey<PendingFoodService>("PendingFoodService")
val UserEdibleServiceKey = AttributeKey<UserEdibleService>("UserEdibleService")
val UserServiceKey = AttributeKey<UserService>("UserService")