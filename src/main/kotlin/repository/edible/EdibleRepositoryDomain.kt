package com.example.repository.edible

import com.example.domain.*
import com.example.mapping.AEDao
import com.example.mapping.EdibleDao

data class EdibleRepoResult<E : EdibleDao, N : NutrientEntry>(
    val edibleDao: E,
    val nutrients: List<N>
)

typealias AppEdibleRepoResult = EdibleRepoResult<AEDao, NutrientDataAmount>

/**
 * Holds required data that the repository needs to perform an update/submission
 */
data class AppEdibleRepoWrite(
    val base: EdibleBase,
    val nutrientList: List<NutrientIdWithAmount>,
    val edibleType: EdibleType
)
