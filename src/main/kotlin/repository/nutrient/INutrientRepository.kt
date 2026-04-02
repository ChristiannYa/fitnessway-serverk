package com.example.repository.nutrient

import com.example.domain.NutrientData
import java.util.*

interface INutrientRepository {
    suspend fun findAllWithData(userId: UUID): List<NutrientData>
}