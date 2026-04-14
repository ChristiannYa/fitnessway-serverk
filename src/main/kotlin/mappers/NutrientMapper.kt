package com.example.mappers

import com.example.constants.NutrientId
import com.example.domain.*
import com.example.mapping.FoodNutrientTable
import com.example.mapping.N
import com.example.mapping.UNP
import org.jetbrains.exposed.sql.ResultRow

fun <N : NutrientGroupable> NutrientsByType<N>.toList() =
    this.basic + this.vitamins + this.minerals

fun <N : NutrientGroupable> List<N>.toCategoryGroups() =
    this
        .groupBy { it.iNutrientType }
        .let {
            NutrientsByType(
                basic = it[NutrientType.BASIC] ?: emptyList(),
                vitamins = it[NutrientType.VITAMIN] ?: emptyList(),
                minerals = it[NutrientType.MINERAL] ?: emptyList()
            )
        }

fun List<NutrientDataAmount>.toClientFilter(
    isAppFood: Boolean = false,
    isUserPremium: Boolean = false
) = this
    .filter { isAppFood || it.nutrientData.preferences.goal != null }
    .filter { isAppFood || isUserPremium || !it.nutrientData.base.isPremium }
    .sortedWith(
        compareBy<NutrientDataAmount> {
            listOf(
                NutrientId.CALORIES,
                NutrientId.CARBS,
                NutrientId.FATS,
                NutrientId.PROTEIN
            )
                .indexOf(it.nutrientData.base.id)
                .let { i -> if (i != -1) i else Int.MAX_VALUE }
        }.thenBy { it.nutrientData.base.id }
    )

fun ResultRow.toNutrientDataAmount(foodNutrientTable: FoodNutrientTable) = NutrientDataAmount(
    nutrientData = NutrientData(
        base = NutrientBase(
            id = this[N.id].value,
            name = this[N.name],
            unit = this[N.unit],
            type = this[N.type],
            symbol = this[N.symbol],
            isPremium = this[N.isPremium]
        ),
        preferences = NutrientPreferences(
            hexColor = this.getOrNull(UNP.hexColor),
            goal = this.getOrNull(UNP.goal)?.toDouble()
        )
    ),
    amount = this[foodNutrientTable.amount].toDouble()
)