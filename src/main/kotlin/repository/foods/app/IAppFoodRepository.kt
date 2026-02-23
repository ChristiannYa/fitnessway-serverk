package com.example.repository.foods.app

import com.example.domain.FoodInformation
import com.example.domain.NutrientIdWithAmount

interface IAppFoodRepository {
    suspend fun isDuplicate(food: FoodInformation<NutrientIdWithAmount>): Boolean
}