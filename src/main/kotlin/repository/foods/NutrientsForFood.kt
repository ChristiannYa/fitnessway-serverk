package com.example.repository.foods

import com.example.domain.NutrientBase
import com.example.domain.NutrientData
import com.example.domain.NutrientInFood
import com.example.domain.NutrientPreferences
import com.example.mapping.FoodNutrientTable
import com.example.repository.N
import com.example.repository.NP
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.selectAll
import java.util.*

fun <T> queryNutrientsForFood(
    foodNutrientTable: T,
    foodId: Int,
    userId: UUID
): List<NutrientInFood> where T : Table, T : FoodNutrientTable {
    return (foodNutrientTable innerJoin N)
        // LEFT JOIN user_nutrient_preferences
        // ON nutrients.id = user_nutrient_preferences.nutrient_id
        // AND user_nutrient_preferences.user_id = ?
        .join(
            joinType = JoinType.LEFT,
            otherTable = NP,
            onColumn = N.id,
            otherColumn = NP.nutrientId,
            additionalConstraint = { NP.userId eq userId }
        )
        .selectAll()
        .where { foodNutrientTable.foodId eq foodId }
        .map { row ->
            NutrientInFood(
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
                        hexColor = row.getOrNull(NP.hexColor),
                        goal = row.getOrNull(NP.goal)?.toDouble()
                    )
                ),
                amount = row[foodNutrientTable.amount].toDouble()
            )
        }
}