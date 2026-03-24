package com.example.mappers

import com.example.domain.NutrientGroupable
import com.example.domain.NutrientType
import com.example.domain.NutrientsByType

fun <N : NutrientGroupable> NutrientsByType<N>.toList() =
    this.basic + this.vitamins + this.minerals

fun <N : NutrientGroupable> List<N>.toType(): NutrientsByType<N> {
    val grouped = this.groupBy { it.nutrientType }

    return NutrientsByType(
        basic = grouped[NutrientType.BASIC] ?: emptyList(),
        vitamins = grouped[NutrientType.VITAMIN] ?: emptyList(),
        minerals = grouped[NutrientType.MINERAL] ?: emptyList()
    )
}