package com.example.service

import com.example.domain.FoodLog
import com.example.domain.FoodLogAdd
import com.example.exception.FoodLogNotFoundException
import com.example.exception.FoodNotFoundException
import com.example.mappers.toNutrientIntakesFromFood
import com.example.repository.foods.log.IFoodLogRepository
import com.example.repository.nutrient.intake.INutrientIntakeRepository
import com.example.utils.suspendTransaction

class FoodLogService(
    private val foodLogRepository: IFoodLogRepository,
    private val nutrientIntakeRepository: INutrientIntakeRepository
) {
    suspend fun findById(id: Int) = foodLogRepository.findById(id)

    suspend fun add(foodLogAddData: FoodLogAdd): FoodLog = suspendTransaction {
        val foodLogId = foodLogRepository.add(foodLogAddData)

        val isNutrientInsertionSuccess = nutrientIntakeRepository.insertFromFood(
            foodLogAddData.toNutrientIntakesFromFood(foodLogId)
        )

        if (!isNutrientInsertionSuccess) {
            throw FoodNotFoundException("no nutrient data found for food with id ${foodLogAddData.foodId}")
        }

        val foodLog = foodLogRepository.findById(foodLogId)
            ?: throw FoodLogNotFoundException()

        foodLog
    }
}