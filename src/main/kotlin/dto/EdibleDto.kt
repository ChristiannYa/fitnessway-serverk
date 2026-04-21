package com.example.dto

import com.example.domain.EdibleBase
import com.example.domain.NutrientDataAmount
import com.example.domain.NutrientIdWithAmount
import com.example.domain.NutrientsByType
import kotlinx.serialization.Serializable

@Serializable
data class FoodInformationDto(
    val base: EdibleBase,
    val nutrients: NutrientsByType<NutrientDataAmount>
)

@Serializable
data class FoodLogAddRequest(
    val edibleId: Int,
    val edibleType: String,
    val servings: Double,
    val category: String,
    val time: String,
    val source: String
)

@Serializable
data class FoodLogUpdateRequest(
    val foodLogId: Int,
    val userFoodSnapshotId: Int?,
    val servings: Double
)

@Serializable
data class UserEdibleAddRequest(
    val base: EdibleBase,
    val nutrients: List<NutrientIdWithAmount>,
    val edibleType: String
)

@Serializable
data class PendingFoodAddRequest(
    val base: EdibleBase,
    val nutrients: List<NutrientIdWithAmount>
)

@Serializable
data class PendingFoodReviewRequest(
    val createdById: String,
    val pendingFoodId: Int,
    val rejectionReason: String?
)