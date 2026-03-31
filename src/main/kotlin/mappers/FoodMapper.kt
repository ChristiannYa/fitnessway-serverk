package com.example.mappers

import com.example.domain.*

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

fun FoodLogAdd.toNutrientIntakesFromFood(foodLogId: Int) = NutrientIntakesFromFood(
    userId = this.userId,
    foodLogId = foodLogId,
    foodId = this.foodId,
    servings = this.servings,
    source = this.source
)