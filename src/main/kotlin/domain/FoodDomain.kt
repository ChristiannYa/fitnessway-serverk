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
enum class LogCategory {
    BREAKFAST,
    LUNCH,
    DINNER,
    SUPPLEMENT
}

@Serializable
enum class EdibleType {
    FOOD,
    SUPPLEMENT
}

@Serializable
enum class LogSource {
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
enum class UserEdibleSnapshotStatus {
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
    val lastLoggedAt: Instant? = null,
    val createdAt: Instant,
    val updatedAt: Instant? = null,
)

@Serializable
data class FoodLog(
    val id: Int,
    val category: LogCategory,
    val time: Instant,
    val loggedAt: Instant,
    val servings: Double,
    val userEdibleSnapshotStatus: UserEdibleSnapshotStatus? = null,
    val userFoodSnapshotId: Int?,
    val source: LogSource,
    val foodId: Int?,
    val foodInformation: FoodInformationDto
)

@Serializable
data class FoodLogsCategorized(
    val breakfast: List<FoodLog>,
    val lunch: List<FoodLog>,
    val dinner: List<FoodLog>,
    val supplement: List<FoodLog>
)

@Serializable
data class FoodPreview(
    val id: Int,
    val base: FoodBase,
    val nutrientPreview: NutrientPreview,
    val source: LogSource
)

data class FoodInformation<N : NutrientEntry>(
    val base: FoodBase,
    val nutrients: List<N>
)

data class FoodLogBase(
    val foodId: Int?,
    val userFoodSnapshotId: Int?,
    val servings: Double,
    val source: LogSource
)

/**
 * Holds the data the **repository** needs in order to add a food log
 */
data class FoodLogAdd(
    val userId: UUID,
    val foodId: Int,
    val servings: Double,
    val category: LogCategory,
    val time: Instant,
    val source: LogSource
)

/**
 * Holds the data the **repository** needs in order to update a food log
 */
data class FoodLogUpdate(
    val userId: UUID,
    val isUserPremium: Boolean,
    val foodLogId: Int,
    val userFoodSnapshotId: Int?,
    val servings: Double
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
    val userPrincipal: UserPrincipal
)

/**
 * Holds the data needed to review a pending food
 */
data class PendingFoodReview(
    val createdById: UUID,
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
 * Represents the criteria by which [AppFood] paginated queries can be filtered
 */
data class AppFoodSearchPaginationCriteria(
    val query: String,
    val userId: UUID
)

/**
 * Represents the criteria by which [PendingFood] paginated queries can be found
 */
data class PendingFoodsPaginationCriteria(
    val userId: UUID,
    val userScope: UserScope,
    val status: PendingFoodStatus? = null
)

/**
 * Represents the criteria by which recently logged foods (as [FoodPreview]) paginated
 * queries can be found
 */
data class RecentlyLoggedFoodsPaginationCriteria(
    val userId: UUID
)

/**
 * Represents the criteria by which [UserFood] paginated queries can be found
 */
data class UserEdiblesPaginationCriteria(
    val userId: UUID,
    val edibleType: EdibleType
)