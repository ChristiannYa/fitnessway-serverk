package com.example.mappers

import com.example.domain.AppFoodCreate
import com.example.domain.FoodInformation
import com.example.domain.NutrientIdWithAmount
import com.example.domain.PendingFood

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