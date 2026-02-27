package com.example.config

import com.example.domain.UserPrincipal
import com.example.service.AuthService
import com.example.service.JwtService
import com.example.service.PendingFoodService
import com.example.service.UserService
import io.ktor.util.*

val UserPrincipalKey = AttributeKey<UserPrincipal>("UserPrincipal")
val JwtServiceKey = AttributeKey<JwtService>("JwtService")
val AuthServiceKey = AttributeKey<AuthService>("AuthService")
val PendingFoodServiceKey = AttributeKey<PendingFoodService>("PendingFoodService")
val UserServiceKey = AttributeKey<UserService>("UserService")