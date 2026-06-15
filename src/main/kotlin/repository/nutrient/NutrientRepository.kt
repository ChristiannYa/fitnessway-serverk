package com.example.repository.nutrient

import com.example.domain.NutrientData
import com.example.mapping.N
import com.example.utils.suspendTransaction
import org.jetbrains.exposed.sql.selectAll
import java.util.*

class NutrientRepository : INutrientRepository {

    override suspend fun getNutrientDataList(
        userId: UUID
    ): List<NutrientData> = suspendTransaction {
        N
            .nutrientDataJoins(userId)
            .selectAll()
            .map { it.toNutrientData() }
    }
}
