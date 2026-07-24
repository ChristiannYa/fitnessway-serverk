package com.example.repository.edible

import com.example.domain.*
import com.example.mapping.AEDao
import com.example.mapping.AERDao
import com.example.mapping.EdibleDao
import java.util.*

data class EdibleRepoResult<E : EdibleDao, N : NutrientEntry>(
    val edibleDao: E,
    val nutrients: List<N>,
)

typealias AppEdibleRepoResult = EdibleRepoResult<AEDao, NutrientDataAmount>

data class AppEdibleRepoWrite(
    val base: EdibleBase,
    val nutrientList: List<NutrientIdWithAmount>,
    val type: EdibleType,
    val barcode: String,
)

data class AppEdibleReportWrite(
    val edibleId: Int,
    val reportedBy: UUID,
    val reasons: List<String>,
    val notes: String?
)

data class AppEdibleReportReview(
    val id: Int,
    val reviewedBy: UUID
)

data class AppEdibleReportQuery(
    val edible: Pair<AppEdibleRepoResult, String>,
    val reports: List<Pair<AERDao, List<AppEdibleReport.Reason>>>,
)
