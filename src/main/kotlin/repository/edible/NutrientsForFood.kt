package com.example.repository.edible

import com.example.constants.NutrientId
import com.example.domain.NutrientAmountWithColor
import com.example.domain.NutrientDataAmount
import com.example.domain.NutrientPreview
import com.example.mappers.toNutrientDataAmount
import com.example.mapping.EdibleNutrientTable
import com.example.mapping.N
import com.example.mapping.UNP
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll
import java.util.*

fun <T> queryNutrientsForFood(
    foodNutrientTable: T,
    foodId: Int,
    userId: UUID
): List<NutrientDataAmount> where T : Table, T : EdibleNutrientTable =
    (foodNutrientTable innerJoin N)
        .join(
            joinType = JoinType.LEFT,
            otherTable = UNP,
            onColumn = N.id,
            otherColumn = UNP.nutrientId,
            additionalConstraint = { UNP.userId eq userId }
        )
        .selectAll()
        .where { foodNutrientTable.edibleId eq foodId }
        .map { row -> row.toNutrientDataAmount(foodNutrientTable) }

fun <T> queryNutrientsForFoods(
    foodNutrientTable: T,
    foodIds: List<Int>,
    userId: UUID
): Map<Int, List<NutrientDataAmount>?> where T : Table, T : EdibleNutrientTable =
    (foodNutrientTable innerJoin N)
        .join(
            joinType = JoinType.LEFT,
            otherTable = UNP,
            onColumn = N.id,
            otherColumn = UNP.nutrientId,
            additionalConstraint = { UNP.userId eq userId }
        )
        .selectAll()
        .where { (foodNutrientTable.edibleId inList foodIds) }
        .groupBy(
            keySelector = { row -> row[foodNutrientTable.edibleId].value },
            valueTransform = { row -> row.toNutrientDataAmount(foodNutrientTable) }
        )

fun <T> queryNutrientPreviews(
    foodNutrientTable: T,
    foodIds: List<Int>,
    userId: UUID
): Map<Int, NutrientPreview> where T : Table, T : EdibleNutrientTable {
    val previewIds = listOf(
        NutrientId.CALORIES,
        NutrientId.CARBS,
        NutrientId.FATS,
        NutrientId.PROTEIN
    )

    val foodRows = (foodNutrientTable innerJoin N)
        .join(
            joinType = JoinType.LEFT,
            otherTable = UNP,
            onColumn = N.id,
            otherColumn = UNP.nutrientId,
            additionalConstraint = { UNP.userId eq userId }
        )
        .selectAll()
        .where {
            (foodNutrientTable.edibleId inList foodIds) and
            (foodNutrientTable.nutrientId inList previewIds)
        }
        .groupBy { row -> row[foodNutrientTable.edibleId].value }

    return foodIds.associateWith { foodId ->
        val nutrientData = foodRows[foodId]
            ?.associate { row ->
                row[foodNutrientTable.nutrientId].value to
                NutrientAmountWithColor(
                    amount = row[foodNutrientTable.amount].toDouble(),
                    color = row.getOrNull(UNP.hexColor)
                )
            }

        NutrientPreview(
            calories = nutrientData?.get(NutrientId.CALORIES) ?: NutrientAmountWithColor(),
            carbs = nutrientData?.get(NutrientId.CARBS) ?: NutrientAmountWithColor(),
            fats = nutrientData?.get(NutrientId.FATS) ?: NutrientAmountWithColor(),
            protein = nutrientData?.get(NutrientId.PROTEIN) ?: NutrientAmountWithColor()
        )
    }
}