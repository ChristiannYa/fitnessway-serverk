package com.example.dto

import com.example.domain.FoodBase
import com.example.domain.NutrientIdWithAmount
import com.example.domain.NutrientInFood
import com.example.domain.NutrientsByType
import kotlinx.serialization.Serializable

@Serializable
data class FoodInformationDto(
    val base: FoodBase,
    val nutrients: NutrientsByType<NutrientInFood>
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