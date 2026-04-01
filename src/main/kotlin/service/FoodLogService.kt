package com.example.service

import com.example.domain.*
import com.example.exception.FoodLogNotFoundException
import com.example.exception.FoodNotFoundException
import com.example.mappers.toCategory
import com.example.mappers.toNutrientIntakesFromFood
import com.example.repository.foods.log.IFoodLogRepository
import com.example.repository.nutrient.intake.INutrientIntakeRepository
import com.example.utils.date_time.DateTimeParser
import com.example.utils.date_time.TimeConverter
import com.example.utils.suspendTransaction
import io.ktor.server.plugins.*
import java.time.format.DateTimeParseException

class FoodLogService(
    private val foodLogRepository: IFoodLogRepository,
    private val nutrientIntakeRepository: INutrientIntakeRepository,
    private val dateTimeParser: DateTimeParser,
    private val userTimeConverter: TimeConverter,
) {
    suspend fun findById(id: Int) = foodLogRepository.findById(id)

    suspend fun findByDate(userPrincipal: UserPrincipal, date: String): FoodLogsCategorized {
        val range = try {
            userTimeConverter.toUtcRange(date, userPrincipal.timezone)
        } catch (_: DateTimeParseException) {
            throw BadRequestException("invalid date format, expected ${dateTimeParser.datePattern}")
        }

        return when (val result = foodLogRepository.findByDate(userPrincipal.id, range)) {
            is FoodLogResult.Success -> result.foodLogs.toCategory()
            is FoodLogResult.Error -> throw IllegalStateException("failed to map food log with id ${result.failedId}")
        }
    }

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