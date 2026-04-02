package com.example.mappers

import com.example.domain.*

fun List<FoodLog>.toCategory(): FoodLogsCategorized {
    val grouped = this.groupBy { it.category }

    return FoodLogsCategorized(
        breakfast = grouped[FoodLogCategory.BREAKFAST] ?: emptyList(),
        lunch = grouped[FoodLogCategory.LUNCH] ?: emptyList(),
        dinner = grouped[FoodLogCategory.DINNER] ?: emptyList(),
        supplement = grouped[FoodLogCategory.SUPPLEMENT] ?: emptyList()
    )
}

/**
 * Maps a [PendingFood] to an [AppFoodCreate] object
 *
 * @return [AppFoodCreate] if the pending food has an author, `null` otherwise
 */
fun PendingFood.toCreate(): AppFoodCreate? = this.createdBy?.let {
    val food = FoodInformation(
        base = this.information.base,
        nutrients = this.information.nutrients.toList().map {
            NutrientIdWithAmount(it.nutrientData.base.id, it.amount)
        }
    )

    AppFoodCreate(food, this.createdBy)
}