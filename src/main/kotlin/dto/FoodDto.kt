package com.example.dto

import com.example.domain.FoodBase
import com.example.domain.NutrientIdWithAmount
import kotlinx.serialization.Serializable

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