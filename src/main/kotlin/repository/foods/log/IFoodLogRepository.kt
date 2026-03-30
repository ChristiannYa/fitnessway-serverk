package com.example.repository.foods.log

import com.example.domain.FoodLog
import com.example.domain.FoodLogAdd

interface IFoodLogRepository {
    suspend fun findById(id: Int): FoodLog?
    suspend fun add(data: FoodLogAdd): Int
}