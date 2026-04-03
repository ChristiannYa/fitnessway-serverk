package com.example.dto

import com.example.domain.*
import kotlinx.serialization.Serializable

@Serializable
data class FoodInformationDto(
    val base: FoodBase,
    val nutrients: NutrientsByType<NutrientDataAmount>
)

@Serializable
data class FoodLogAddRequest(
    val foodId: Int,
    val servings: Double,
    val category: FoodLogCategory,
    val time: String,
    val source: FoodSource
)

@Serializable
data class FoodLogUpdateRequest(
    val foodLogId: Int,
    val userFoodSnapshotId: Int?,
    val servings: Double
)

@Serializable
data class PendingFoodAddRequest(
    val base: FoodBase,
    val nutrients: List<NutrientIdWithAmount>
)

@Serializable
data class PendingFoodReviewRequest(
    val pendingFoodId: Int,
    val rejectionReason: String?
)