package com.example.dto

import com.example.domain.*
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class FoodInformationDto(
    val base: FoodBase,
    val nutrients: NutrientsByType<NutrientInFood>
)

@Serializable
data class FoodLogAddRequest(
    val foodId: Int,
    val servings: Double,
    val category: FoodLogCategory,
    val time: Instant,
    val source: FoodSource
)

@Serializable
data class AddPendingFoodRequest(
    val base: FoodBase,
    val nutrients: List<NutrientIdWithAmount>
)

@Serializable
data class ReviewPendingFoodRequest(
    val pendingFoodId: Int,
    val rejectionReason: String?
)