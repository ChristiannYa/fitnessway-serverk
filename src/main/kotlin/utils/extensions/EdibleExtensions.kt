package com.example.utils.extensions

import com.example.domain.NutrientDataAmount

fun List<NutrientDataAmount>.filterAccesibility(
    isAppFood: Boolean = false,
    isUserPremium: Boolean = false
) = this
    .filter { it.data.preferences.goal != null }
    .filter { isAppFood || isUserPremium || !it.data.base.isPremium }
