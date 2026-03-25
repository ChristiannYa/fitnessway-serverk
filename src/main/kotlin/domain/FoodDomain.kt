// Apply serializer to all usages of UUID
@file:UseSerializers(UUIDSerializer::class)

package com.example.domain

import com.example.dto.FoodInformationDto
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
enum class FoodLogCategory {
    BREAKFAST,
    LUNCH,
    DINNER,
    SUPPLEMENT
}

@Serializable
enum class FoodSource {
    USER,
    APP
}

@Serializable
enum class PendingFoodStatus {
    PENDING,
    APPROVED,
    REJECTED;

    val isReviewed by lazy { this != PENDING }
}

@Serializable
enum class UserFoodSnapshotStatus {
    PRESENT,
    UPDATED,
    DELETED
}

@Serializable
data class FoodBase(
    val name: String,
    val brand: String? = null,
    val amountPerServing: Double,
    val servingUnit: ServingUnit
)

@Serializable
data class AppFood(
    val id: Int,
    val information: FoodInformationDto,
    val createdBy: UUID?,
    val createdAt: Instant,
    val updatedAt: Instant? = null
)

@Serializable
data class PendingFood(
    val id: Int,
    val information: FoodInformationDto,
    val status: PendingFoodStatus,
    val createdBy: UUID?,
    val reviewedBy: UUID? = null,
    val reviewedAt: Instant? = null,
    val createdAt: Instant,
    val rejectionReason: String? = null,
)

@Serializable
data class UserFood(
    val id: Int,
    val information: FoodInformationDto,
    val isFavorite: Boolean,
    val lastLoggedAt: Instant? = null,
    val createdAt: Instant,
    val updatedAt: Instant? = null,
)

@Serializable
data class FoodSearchResult(
    val id: Int,
    val base: FoodBase,
    val nutrientsPreview: NutrientPreview
)

data class FoodInformation<N : NutrientEntry>(
    val base: FoodBase,
    val nutrients: List<N>
)

/**
 * Holds the data needed to create an app food
 */
data class AppFoodCreate(
    val food: FoodInformation<NutrientIdWithAmount>,
    val createdBy: UUID
)

/**
 * Holds the data needed to create a pending food/app food request
 */
data class PendingFoodCreate(
    val foodInformation: FoodInformation<NutrientIdWithAmount>,
    val author: UUID
)

/**
 * Holds the data needed to review a pending food
 */
data class PendingFoodReview(
    val pendingFoodId: Int,
    val reviewerPrincipal: UserPrincipal,
    val rejectionReason: String?
) {
    val isApproved = this.rejectionReason == null

    val approvalStatus = if (this.isApproved) {
        PendingFoodStatus.APPROVED
    } else PendingFoodStatus.REJECTED
}

/**
 * Represents the criteria by which [AppFoodSearchPaginationCriteria] paginated queries can be filtered
 */
data class AppFoodSearchPaginationCriteria(
    val query: String,
    val userId: UUID
)

/**
 * Represents the criteria by which [PendingFood] paginated queries can be filtered
 */
data class PendingFoodsPaginationCriteria(
    val userScope: UserScope,
    val status: PendingFoodStatus? = null
)