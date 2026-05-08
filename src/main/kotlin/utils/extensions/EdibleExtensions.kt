package com.example.utils.extensions

import com.example.constants.NutrientId
import com.example.domain.NutrientDataAmount

fun List<NutrientDataAmount>.filterAccesibility(
    isAppFood: Boolean = false,
    isUserPremium: Boolean = false
) = this
    .filter { it.data.preferences.goal != null }
    .filter { isAppFood || isUserPremium || !it.data.base.isPremium }

fun List<NutrientDataAmount>.sortBaseNutrients() =
    this.sortedWith(
        compareBy<NutrientDataAmount> { nutrient ->
            listOf<Int>(
                NutrientId.CALORIES,
                NutrientId.CARBS,
                NutrientId.FATS,
                NutrientId.PROTEIN
            )
                .indexOf(nutrient.data.base.id)
                .let { if (it != -1) it else Int.MAX_VALUE }

        }.thenBy { it.data.base.id }
    )