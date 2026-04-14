package com.example.repository.nutrient.intake

import com.example.domain.*
import java.util.*

interface INutrientIntakeRepository {
    suspend fun findByDate(
        userId: UUID,
        isUserPremium: Boolean,
        range: InstantRange,
        nutrientDataList: List<NutrientData>
    ): NutrientIntakes

    suspend fun findByFoodLog(userId: UUID, foodLogId: Int): List<NutrientIntakeRow>
    suspend fun insertFromFood(data: NutrientIntakesFromFood): Boolean
    suspend fun insertFromCurrent(data: NutrientIntakesFromCurrent)
    suspend fun deleteByFoodLog(userId: UUID, foodLogId: Int): Boolean
}