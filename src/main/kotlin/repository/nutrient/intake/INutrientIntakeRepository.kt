package com.example.repository.nutrient.intake

import com.example.domain.NutrientIntakeRecalculate
import com.example.domain.NutrientIntakesFromFood

interface INutrientIntakeRepository {
    suspend fun insertFromFood(data: NutrientIntakesFromFood): Boolean
    suspend fun recalculate(data: NutrientIntakeRecalculate)
}