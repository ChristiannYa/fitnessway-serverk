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
enum class PendingFoodStatus {
    PENDING,
    APPROVED,
    REJECTED;

    fun isReviewed() = this != PENDING
    fun isApproved() = this == APPROVED
    fun isRejected() = this == REJECTED
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
    val information: FoodInformation<NutrientInFood>,
    val createdBy: UUID?, // User could delete their account
    val createdAt: Instant,
    val updatedAt: Instant? = null
)

@Serializable
data class PendingFood(
    val id: Int,
    val information: FoodInformation<NutrientInFood>,
    val createdBy: UUID,
    val status: PendingFoodStatus,
    val reviewedBy: UUID? = null,
    val reviewedAt: Instant? = null,
    val createdAt: Instant,
    val rejectionReason: String? = null,
)

data class PendingFoodCreate(
    val foodInformation: FoodInformation<NutrientIdWithAmount>,
    val submittedBy: UUID
)

data class PendingFoodReview(
    val pendingFoodId: Int,
    val reviewerId: UUID,
    val rejectionReason: String?
) {
    fun isApproved() = this.rejectionReason == null

    fun getApprovalStatus() = if (this.isApproved()) {
        PendingFoodStatus.APPROVED
    } else PendingFoodStatus.REJECTED
}