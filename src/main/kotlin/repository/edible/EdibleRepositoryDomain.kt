package com.example.repository.edible

import com.example.domain.NutrientDataAmount
import com.example.domain.NutrientEntry
import com.example.mapping.AEDao
import com.example.mapping.EdibleDao

data class EdibleRepoResult<E : EdibleDao, N : NutrientEntry>(
    val edibleDao: E,
    val nutrients: List<N>
)

typealias AppEdibleRepoResult = EdibleRepoResult<AEDao, NutrientDataAmount>
