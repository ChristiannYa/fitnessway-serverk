package com.example.service

import com.example.domain.NutrientData
import com.example.domain.NutrientIntakes
import com.example.domain.NutrientsByType
import com.example.domain.UserPrincipal
import com.example.mappers.toNutrientsByType
import com.example.repository.nutrient.INutrientRepository
import com.example.repository.nutrient.intake.INutrientIntakeRepository
import com.example.utils.date_time.TimeConverter
import com.example.utils.extensions.filterAccesibility
import com.example.utils.extensions.sortBaseNutrients
import io.ktor.server.plugins.*
import java.util.*

class NutrientService(
    private val nutrientRepository: INutrientRepository,
    private val nutrientIntakeRepository: INutrientIntakeRepository,
    private val timeConverter: TimeConverter,
) {

    suspend fun getAllByType(userId: UUID): NutrientsByType<NutrientData> =
        nutrientRepository
            .getNutrientDataList(userId)
            .sortBaseNutrients()
            .toNutrientsByType()

    suspend fun findIntakesByDate(userPrincipal: UserPrincipal, date: String): NutrientIntakes {
        val range = timeConverter
            .toUtcRangeResult(date, userPrincipal.timezone)
            .getOrElse { ex -> throw BadRequestException(ex.message ?: "user time convertion failed") }

        val nutrientDataList = nutrientRepository.getNutrientDataList(userPrincipal.id)

        return nutrientIntakeRepository
            .findByDate(userPrincipal.id, range, nutrientDataList)
            .filterAccesibility(isUserPremium = userPrincipal.isPremium)
            .sortBaseNutrients()
            .toNutrientsByType()
    }
}