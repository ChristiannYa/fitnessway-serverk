package com.example.repository.foods.log

import com.example.domain.FoodLog
import com.example.domain.FoodLogAdd
import com.example.domain.FoodLogResult
import com.example.domain.InstantRange
import java.util.*

interface IFoodLogRepository {
    suspend fun findById(id: Int): FoodLog?
    suspend fun findByDate(userId: UUID, range: InstantRange): FoodLogResult
    suspend fun add(data: FoodLogAdd): Int
}