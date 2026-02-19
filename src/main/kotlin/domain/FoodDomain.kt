// Apply serializer to all usages of UUID
@file:UseSerializers(UUIDSerializer::class)

package com.example.domain

import com.example.utils.UUIDSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import java.util.*
import kotlin.time.Instant

@Serializable
enum class ServingUnit {
    G,
    MG,
    MCG,
    ML,
    OZ,
    KCAL
}

@Serializable
enum class AppFoodPendingStatus {
    PENDING,
    APPROVED,
    REJECTED
}

@Serializable
data class FoodBase(
    val name: String,
    val brand: String? = null,
    val amountPerServing: Double,
    val servingUnit: ServingUnit
)

@Serializable
data class FoodInformation<N : NutrientGeneric>(
    val base: FoodBase,
    val nutrients: List<N>
)

@Serializable
data class AppFood(
    val id: Int,
    val base: FoodInformation<NutrientInFood>,
    val createdBy: UUID?, // User could delete their account
    val createdAt: Instant,
    val updatedAt: Instant? = null
)

@Serializable
data class PendingFood(
    val id: Int,
    val base: FoodInformation<NutrientInFood>,
    val submittedBy: UUID,
    val status: AppFoodPendingStatus,
    val reviewedBy: UUID? = null,
    val reviewedAt: Instant? = null,
    val createdAt: Instant,
    val rejectionReason: String? = null,
)

data class PendingFoodCreate(
    val information: FoodInformation<NutrientIdWithAmount>,
    val submittedBy: UUID
)