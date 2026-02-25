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

    val isReviewed by lazy { this != PENDING }
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
    val createdBy: UUID?,
    val createdAt: Instant,
    val updatedAt: Instant? = null
)

@Serializable
data class PendingFood(
    val id: Int,
    val information: FoodInformation<NutrientInFood>,
    val status: PendingFoodStatus,
    val createdBy: UUID?,
    val reviewedBy: UUID? = null,
    val reviewedAt: Instant? = null,
    val createdAt: Instant,
    val rejectionReason: String? = null,
)

// TODO: Add KDoc comment
data class AppFoodCreate(
    val food: FoodInformation<NutrientIdWithAmount>,
    val createdBy: UUID
)

// TODO: Add KDoc comment
data class PendingFoodCreate(
    val foodInformation: FoodInformation<NutrientIdWithAmount>,
    val author: UUID
)

// TODO: Add KDoc comment
data class PendingFoodReview(
    val pendingFoodId: Int,
    val reviewerPrincipal: UserPrincipal,
    val rejectionReason: String?
) {
    val isApproved = this.rejectionReason == null

    val approvalStatus = if (this.isApproved) {
        PendingFoodStatus.APPROVED
    } else PendingFoodStatus.REJECTED

    val canReview = reviewerPrincipal.type == UserType.ADMIN
}

/**
 * @return `AppFoodCreate` if the pending food has an author
 */
fun PendingFood.toCreate() = this.createdBy?.let {
    val food = FoodInformation(
        base = this.information.base,
        nutrients = this.information.nutrients.map {
            NutrientIdWithAmount(it.nutrientData.base.id, it.amount)
        }
    )

    AppFoodCreate(food, this.createdBy)
}

/**
 * Holds the criteria needed to perform the paginated query
 */
data class PendingFoodsPaginationCriteria(val userType: UserType)