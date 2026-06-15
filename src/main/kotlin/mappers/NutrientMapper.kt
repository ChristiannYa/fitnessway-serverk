package com.example.mappers

import com.example.domain.NutrientGroupable
import com.example.domain.NutrientType
import com.example.domain.NutrientsByType

fun <N : NutrientGroupable> NutrientsByType<N>.toList() =
    this.basic + this.vitamin + this.mineral

fun <N : NutrientGroupable> List<N>.toNutrientsByType() =
    this
        .groupBy { it.byType }
        .let {
            NutrientsByType(
                basic = it[NutrientType.BASIC] ?: emptyList(),
                vitamin = it[NutrientType.VITAMIN] ?: emptyList(),
                mineral = it[NutrientType.MINERAL] ?: emptyList()
            )
        }
