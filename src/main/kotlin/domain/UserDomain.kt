// Apply serializer to all usages of UUID
@file:UseSerializers(UUIDSerializer::class)

package com.example.domain

import com.example.utils.UUIDSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import java.util.*
import kotlin.time.Instant

@Serializable
enum class UserType {
    ADMIN,
    CONTRIBUTOR,
    USER
}

@Serializable
enum class UserTransactionType {
    FOOD_APPROVAL,
    REDEEM,
    FOOD_LOGGED
}

@Serializable
data class User(
    val id: UUID,
    val name: String,
    val email: String,
    val passwordHash: String,
    val isPremium: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant? = null,
    val type: UserType,
)

data class UserPrincipal(
    val id: UUID,
    val type: UserType,
    val isPremium: Boolean
)

data class UserCreate(
    val name: String,
    val email: String,
    val passwordHash: String,
    val userType: UserType = UserType.USER
)

data class UserAddCurrency(
    val userId: UUID,
    val amount: Double,
    val transactionType: UserTransactionType
)

fun User.toPrincipal() = UserPrincipal(
    this.id,
    this.type,
    this.isPremium
)