package com.example.repository.nutrient.intake

import com.example.domain.*
import com.example.mappers.toCategoryGroups
import com.example.mappers.toClientFilter
import com.example.mapping.*
import com.example.utils.suspendTransaction
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.math.BigDecimal
import java.time.ZoneOffset
import java.util.*
import kotlin.time.toJavaInstant

class NutrientIntakeRepository : INutrientIntakeRepository {
    override suspend fun findByDate(
        userId: UUID,
        isUserPremium: Boolean,
        range: InstantRange,
        nutrientDataList: List<NutrientData>
    ): NutrientIntakes = suspendTransaction {

        val intakesMap = (UEL innerJoin UNI innerJoin N)
            .select(N.id, UNI.intakeAmount.sum())
            .where {
                (UEL.userId eq userId) and
                (UEL.time greaterEq range.start.toJavaInstant().atOffset(ZoneOffset.UTC)) and
                (UEL.time less range.end.toJavaInstant().atOffset(ZoneOffset.UTC))
            }
            .groupBy(N.id)
            .associate { row ->
                row[N.id].value to (row[UNI.intakeAmount.sum()]?.toDouble() ?: 0.0)
            }

        nutrientDataList.map { nutrientData ->
            NutrientDataAmount(
                nutrientData = nutrientData,
                amount = intakesMap[nutrientData.base.id] ?: 0.0
            )
        }
            .toClientFilter(isUserPremium = isUserPremium)
            .toCategoryGroups()
    }

    override suspend fun findByFoodLog(
        userId: UUID,
        foodLogId: Int
    ): List<NutrientIntakeRow> = suspendTransaction {
        UNI.selectAll()
            .where { (UNI.userId eq userId) and (UNI.edibleLogId eq foodLogId) }
            .map { row ->
                NutrientIntakeRow(
                    nutrientId = row[UNI.nutrientId].value,
                    amount = row[UNI.intakeAmount]
                )
            }
    }

    override suspend fun insertFromFood(data: NutrientIntakesFromFood): Boolean = suspendTransaction {
        val nutrientsTable = when (data.source) {
            LogSource.APP -> AFN
            LogSource.USER -> UEN
        }

        val nutrients = nutrientsTable.queryFoodNutrientAmounts(data.foodId, data.servings)
            ?: return@suspendTransaction false

        UNI.batchInsert(nutrients) { (nutrientId, amount) ->
            this[UNI.userId] = EntityID(data.userId, U)
            this[UNI.edibleLogId] = EntityID(data.foodLogId, UEL)
            this[UNI.nutrientId] = EntityID(nutrientId, N)
            this[UNI.intakeAmount] = amount
        }

        return@suspendTransaction true
    }

    override suspend fun insertFromCurrent(data: NutrientIntakesFromCurrent): Unit = suspendTransaction {
        val ratio = data.newServings / data.curServings

        UNI.batchInsert(data.curIntakes) { intake ->
            this[UNI.userId] = data.userId
            this[UNI.edibleLogId] = data.foodLogId
            this[UNI.nutrientId] = intake.nutrientId
            this[UNI.intakeAmount] = (intake.amount * ratio.toBigDecimal())
        }
    }

    override suspend fun deleteByFoodLog(userId: UUID, foodLogId: Int): Boolean = suspendTransaction {
        val deleteCount = UNI.deleteWhere {
            (UNI.edibleLogId eq foodLogId) and (UNI.userId eq userId)
        }

        deleteCount > 0
    }

    private fun <T> T.queryFoodNutrientAmounts(
        foodId: Int,
        servings: Double
    ): List<Pair<Int, BigDecimal>>? where T : Table, T : EdibleNutrientTable {
        val nutrients = this
            .selectAll()
            .where { this@queryFoodNutrientAmounts.edibleId eq foodId }
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