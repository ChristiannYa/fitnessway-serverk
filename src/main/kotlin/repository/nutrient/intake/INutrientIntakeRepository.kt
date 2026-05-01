package com.example.repository.nutrient.intake

import com.example.domain.*
import java.util.*

interface INutrientIntakeRepository {
    suspend fun findByDate(
        userId: UUID,
        range: InstantRange,
        nutrientDataList: List<NutrientData>
    ): List<NutrientDataAmount>

    suspend fun findByFoodLog(userId: UUID, foodLogId: Int): List<NutrientIntakeRow>
    suspend fun insertFromFood(data: NutrientIntakesFromFood): Boolean
    suspend fun insertFromCurrent(data: NutrientIntakesFromCurrent)
    suspend fun deleteByFoodLog(userId: UUID, foodLogId: Int): Boolean
}