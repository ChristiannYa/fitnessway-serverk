package com.example.repository.foods.log

import com.example.domain.FoodLog
import com.example.domain.FoodLogAdd
import com.example.domain.FoodLogBase
import com.example.domain.InstantRange
import java.util.*

interface IFoodLogRepository {
    suspend fun findById(userId: UUID, id: Int): FoodLog?
    suspend fun findByDate(userId: UUID, range: InstantRange): Result<List<FoodLog>>
    suspend fun getBaseData(userId: UUID, foodLogId: Int): FoodLogBase?
    suspend fun add(data: FoodLogAdd): Int
    suspend fun updateServings(userId: UUID, foodLogId: Int, servings: Double): Boolean
}