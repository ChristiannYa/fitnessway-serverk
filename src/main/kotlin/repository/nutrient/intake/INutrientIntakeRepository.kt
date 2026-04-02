package com.example.repository.nutrient.intake

import com.example.domain.NutrientIntakeRow
import com.example.domain.NutrientIntakesFromCurrent
import com.example.domain.NutrientIntakesFromFood
import java.util.*

interface INutrientIntakeRepository {
    suspend fun findByFoodLog(userId: UUID, foodLogId: Int): List<NutrientIntakeRow>
    suspend fun insertFromFood(data: NutrientIntakesFromFood): Boolean
    suspend fun insertFromCurrent(data: NutrientIntakesFromCurrent)
    suspend fun deleteByFoodLog(userId: UUID, foodLogId: Int): Boolean
}