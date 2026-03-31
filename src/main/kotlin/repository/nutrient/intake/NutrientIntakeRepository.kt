package com.example.repository.nutrient.intake

import com.example.domain.FoodSource
import com.example.domain.NutrientIntakeRecalculate
import com.example.domain.NutrientIntakesFromFood
import com.example.mapping.*
import com.example.utils.suspendTransaction
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.selectAll
import java.math.BigDecimal

class NutrientIntakeRepository : INutrientIntakeRepository {
    override suspend fun insertFromFood(data: NutrientIntakesFromFood): Boolean = suspendTransaction {
        val nutrientsTable = when (data.source) {
            FoodSource.APP -> AFN
            FoodSource.USER -> UFN
        }

        val nutrients = nutrientsTable.queryNutrientAmounts(data.foodId, data.servings)
            ?: return@suspendTransaction false

        UNI.batchInsert(nutrients) { (nutrientId, intakeAmount) ->
            this[UNI.userId] = EntityID(data.userId, U)
            this[UNI.foodLogId] = EntityID(data.foodLogId, UFL)
            this[UNI.nutrientId] = EntityID(nutrientId, N)
            this[UNI.intakeAmount] = intakeAmount
        }

        return@suspendTransaction true
    }

    override suspend fun recalculate(data: NutrientIntakeRecalculate) {
        TODO("Not yet implemented")
    }

    private fun <T> T.queryNutrientAmounts(
        foodId: Int,
        servings: Double
    ): List<Pair<Int, BigDecimal>>? where T : Table, T : FoodNutrientTable {
        val nutrients = this
            .selectAll()
            .where { this@queryNutrientAmounts.foodId eq foodId }
            .map { row ->
                Pair(
                    row[nutrientId].value,
                    (row[this@queryNutrientAmounts.amount] * servings.toBigDecimal())
                )
            }

        if (nutrients.isEmpty()) return null
        return nutrients
    }
}