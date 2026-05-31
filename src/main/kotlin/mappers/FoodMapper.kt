package com.example.mappers

import com.example.domain.*
import com.example.dto.EdibleAddRequest
import com.example.dto.PendingFoodReviewRequest

fun List<FoodLog>.toCategory(): FoodLogsCategorized {
    val grouped = this.groupBy { it.category }

    return FoodLogsCategorized(
        breakfast = grouped[LogCategory.BREAKFAST] ?: emptyList(),
        lunch = grouped[LogCategory.LUNCH] ?: emptyList(),
        dinner = grouped[LogCategory.DINNER] ?: emptyList(),
        supplement = grouped[LogCategory.SUPPLEMENT] ?: emptyList()
    )
}

/**
 * Maps a [PendingFood] to an [AppFoodCreate] object
 *
 * @return [AppFoodCreate] if the pending food has an author, `null` otherwise
 */
fun PendingFood.toCreate(): AppFoodCreate? = this.createdBy?.let { userId ->
    AppFoodCreate(
        createdBy = userId,
        base = this.information.base,
        nutrientList = this.information.nutrients
            .toList()
            .map {
                NutrientIdWithAmount(it.data.base.id, it.amount)
            },
        edibleType = this.edibleType
    )
}

fun PendingFoodCreate.toAddRequest() = EdibleAddRequest(
    base = this.base,
    nutrients = this.nutrientList,
    edibleType = this.edibleType.toString()
)

fun PendingFoodReview.toRequest() = PendingFoodReviewRequest(
    createdById = this.createdById.toString(),
    pendingFoodId = this.pendingFoodId,
    rejectionReason = this.rejectionReason
)