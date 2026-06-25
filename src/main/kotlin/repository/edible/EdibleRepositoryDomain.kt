package com.example.repository.edible

import com.example.domain.*
import com.example.mapping.AEDao
import com.example.mapping.EdibleDao
import java.util.*

data class EdibleRepoResult<E : EdibleDao, N : NutrientEntry>(
    val edibleDao: E,
    val nutrients: List<N>,
)

typealias AppEdibleRepoResult = EdibleRepoResult<AEDao, NutrientDataAmount>

data class AppEdibleWrite(
    val base: EdibleBase,
    val nutrientList: List<NutrientIdWithAmount>,
    val edibleType: EdibleType
)

data class AppEdibleReportWrite(
    val edibleId: Int,
    val reportedBy: UUID,
    val reason: String,
    val notes: String?
)

data class AppEdibleReportReview(
    val id: Int,
    val reviewedBy: UUID
)
