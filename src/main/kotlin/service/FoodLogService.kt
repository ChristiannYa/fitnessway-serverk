package com.example.service

import com.example.repository.foods.log.IFoodLogRepository

class FoodLogService(private val foodLogRepository: IFoodLogRepository) {
    suspend fun findById(id: Int) = foodLogRepository.findById(id)
}