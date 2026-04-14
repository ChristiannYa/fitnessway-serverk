package com.example.mappers

import com.example.constants.NutrientId
import com.example.domain.NutrientDataAmount
import com.example.domain.NutrientGroupable
import com.example.domain.NutrientType
import com.example.domain.NutrientsByType

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