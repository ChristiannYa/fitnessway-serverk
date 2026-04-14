package com.example.service

import com.example.domain.NutrientIntakes
import com.example.domain.UserPrincipal
import com.example.repository.nutrient.INutrientRepository
import com.example.repository.nutrient.intake.INutrientIntakeRepository
import com.example.utils.date_time.TimeConverter
import io.ktor.server.plugins.*

class NutrientIntakeService(
    private val nutrientRepository: INutrientRepository,
    private val nutrientIntakeRepository: INutrientIntakeRepository,
    private val timeConverter: TimeConverter,
) {
    suspend fun findByDate(userPrincipal: UserPrincipal, date: String): NutrientIntakes {
        val range = timeConverter
            .toUtcRangeResult(date, userPrincipal.timezone)
            .getOrElse { ex -> throw BadRequestException(ex.message ?: "user time convertion failed") }

        val nutrientDataList = nutrientRepository.findAllWithData(userPrincipal.id)

        return nutrientIntakeRepository.findByDate(
            userPrincipal.id,
            userPrincipal.isPremium,
            range,
            nutrientDataList
        )
    }
}