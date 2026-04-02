package com.example.repository.foods

import com.example.constants.NutrientId
import com.example.domain.*
import com.example.mapping.FoodNutrientTable
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
): List<NutrientDataAmount> where T : Table, T : FoodNutrientTable {
    return (foodNutrientTable innerJoin N)
        // LEFT JOIN user_nutrient_preferences
        // ON nutrients.id = user_nutrient_preferences.nutrient_id
        // AND user_nutrient_preferences.user_id = ?
        .join(
            joinType = JoinType.LEFT,
            otherTable = UNP,
            onColumn = N.id,
            otherColumn = UNP.nutrientId,
            additionalConstraint = { UNP.userId eq userId }
        )
        .selectAll()
        .where { foodNutrientTable.foodId eq foodId }
        .map { row ->
            NutrientDataAmount(
                nutrientData = NutrientData(
                    base = NutrientBase(
                        id = row[N.id].value,
                        name = row[N.name],
                        unit = row[N.unit],
                        type = row[N.type],
                        symbol = row[N.symbol],
                        isPremium = row[N.isPremium]
                    ),
                    preferences = NutrientPreferences(
                        hexColor = row.getOrNull(UNP.hexColor),
                        goal = row.getOrNull(UNP.goal)?.toDouble()
                    )
                ),
                amount = row[foodNutrientTable.amount].toDouble()
            )
        }
}

fun <T> queryNutrientPreviews(
    foodNutrientTable: T,
    foodIds: List<Int>,
    userId: UUID
): Map<Int, NutrientPreview> where T : Table, T : FoodNutrientTable {
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
            (foodNutrientTable.foodId inList foodIds) and
            (foodNutrientTable.nutrientId inList previewIds)
        }
        .groupBy { row -> row[foodNutrientTable.foodId].value }

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