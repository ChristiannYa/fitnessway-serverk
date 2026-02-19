package com.example.config

import com.example.domain.UserPrincipal
import com.example.repository.refresh.IRefreshRepository
import com.example.repository.user.IUserRepository
import com.example.service.AuthService
import com.example.service.JwtService
import com.example.service.PendingFoodService
import io.ktor.util.*

val UserPrincipalKey = AttributeKey<UserPrincipal>("UserPrincipal")
val UserRepositoryKey = AttributeKey<IUserRepository>("UserRepository")
val RefreshRepositoryKey = AttributeKey<IRefreshRepository>("RefreshRepository")
val JwtServiceKey = AttributeKey<JwtService>("JwtService")
val AuthServiceKey = AttributeKey<AuthService>("AuthService")
val PendingFoodServiceKey = AttributeKey<PendingFoodService>("PendingFoodService")