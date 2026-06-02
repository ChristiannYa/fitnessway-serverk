package com.example.utils.extensions

import com.example.constants.NutrientId
import com.example.domain.NutrientDataAmount
import com.example.domain.NutrientGroupable

fun List<NutrientDataAmount>.filterAccesibility(
    isAppFood: Boolean = false,
    isUserPremium: Boolean = false
) = this
    .filter { it.data.preferences.goal != null }
    .filter { isAppFood || isUserPremium || !it.data.base.isPremium }

fun <T : NutrientGroupable> List<T>.sortBaseNutrients() =
    this
        .sortedWith(
            compareBy<T> { nutrient ->
                listOf<Int>(
                    NutrientId.CALORIES,
                    NutrientId.CARBS,
                    NutrientId.FATS,
                    NutrientId.PROTEIN
                )
                    .indexOf(nutrient.byId)
                    .let { if (it != -1) it else Int.MAX_VALUE }

            }.thenBy { it.byId }
        )