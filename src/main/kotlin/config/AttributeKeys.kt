package com.example.config

import com.example.domain.UserPrincipal
import com.example.service.*
import io.ktor.util.*

val UserPrincipalKey = AttributeKey<UserPrincipal>("UserPrincipal")
val JwtServiceKey = AttributeKey<JwtService>("JwtService")
val AuthServiceKey = AttributeKey<AuthService>("AuthService")
val AppFoodServiceKey = AttributeKey<AppFoodService>("AppFoodService")
val PendingFoodServiceKey = AttributeKey<PendingFoodService>("PendingFoodService")
val UserServiceKey = AttributeKey<UserService>("UserService")