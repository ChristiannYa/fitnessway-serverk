package com.example.repository.nutrient.intake

import com.example.domain.FoodSource
import com.example.domain.NutrientIntakeRow
import com.example.domain.NutrientIntakesFromCurrent
import com.example.domain.NutrientIntakesFromFood
import com.example.mapping.*
import com.example.utils.suspendTransaction
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.math.BigDecimal
import java.util.*

class NutrientIntakeRepository : INutrientIntakeRepository {
    override suspend fun findByFoodLog(
        userId: UUID,
        foodLogId: Int
    ): List<NutrientIntakeRow> = suspendTransaction {
        UNI.selectAll()
            .where { (UNI.userId eq userId) and (UNI.foodLogId eq foodLogId) }
            .map { row ->
                NutrientIntakeRow(
                    nutrientId = row[UNI.nutrientId].value,
                    amount = row[UNI.intakeAmount]
                )
            }
    }

    override suspend fun insertFromFood(data: NutrientIntakesFromFood): Boolean = suspendTransaction {
        val nutrientsTable = when (data.source) {
            FoodSource.APP -> AFN
            FoodSource.USER -> UFN
        }

        val nutrients = nutrientsTable.queryFoodNutrientAmounts(data.foodId, data.servings)
            ?: return@suspendTransaction false

        UNI.batchInsert(nutrients) { (nutrientId, amount) ->
            this[UNI.userId] = EntityID(data.userId, U)
            this[UNI.foodLogId] = EntityID(data.foodLogId, UFL)
            this[UNI.nutrientId] = EntityID(nutrientId, N)
            this[UNI.intakeAmount] = amount
        }

        return@suspendTransaction true
    }

    override suspend fun insertFromCurrent(data: NutrientIntakesFromCurrent): Unit = suspendTransaction {
        val ratio = data.newServings / data.curServings

        UNI.batchInsert(data.curIntakes) { intake ->
            this[UNI.userId] = data.userId
            this[UNI.foodLogId] = data.foodLogId
            this[UNI.nutrientId] = intake.nutrientId
            this[UNI.intakeAmount] = (intake.amount * ratio.toBigDecimal())
        }
    }

    override suspend fun deleteByFoodLog(userId: UUID, foodLogId: Int): Boolean = suspendTransaction {
        val deleteCount = UNI.deleteWhere {
            (UNI.foodLogId eq foodLogId) and (UNI.userId eq userId)
        }

        deleteCount > 0
    }

    private fun <T> T.queryFoodNutrientAmounts(
        foodId: Int,
        servings: Double
    ): List<Pair<Int, BigDecimal>>? where T : Table, T : FoodNutrientTable {
        val nutrients = this
            .selectAll()
            .where { this@queryFoodNutrientAmounts.foodId eq foodId }
            .map { row ->
                Pair(
                    row[nutrientId].value,
                    (row[this@queryFoodNutrientAmounts.amount] * servings.toBigDecimal())
                )
            }

        if (nutrients.isEmpty()) return null
        return nutrients
    }
}