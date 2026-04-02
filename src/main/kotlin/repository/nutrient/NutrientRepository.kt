package com.example.repository.nutrient

import com.example.domain.NutrientData
import com.example.mapping.N
import com.example.mapping.UNP
import com.example.mapping.toBase
import com.example.mapping.toNutrientPreferences
import com.example.utils.suspendTransaction
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.selectAll
import java.util.*

class NutrientRepository : INutrientRepository {
    override suspend fun findAllWithData(userId: UUID): List<NutrientData> = suspendTransaction {
        N
            .join(
                otherTable = UNP,
                joinType = JoinType.LEFT,
                onColumn = N.id,
                otherColumn = UNP.nutrientId,
                additionalConstraint = { UNP.userId eq userId }
            )
            .selectAll()
            .map { row ->
                NutrientData(
                    base = N.toBase(row),
                    preferences = UNP.toNutrientPreferences(row)
                )
            }
    }
}