package com.example.dto

import com.example.domain.*
import kotlinx.serialization.Serializable

@Serializable
data class FoodInformationDto(
    val base: EdibleBase,
    val nutrients: NutrientsByType<NutrientDataAmount>,
    val type: EdibleType
)

@Serializable
data class EdibleLogAddRequest(
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
data class EdibleWriteRequest(
    val base: EdibleBase,
    val nutrients: List<NutrientIdWithAmount>,
    val edibleType: String
)

@Serializable
data class AppEdibleWriteRequest(
    val edibleRequest: EdibleWriteRequest,
    val barcode: String
)

@Serializable
data class PendingFoodReviewRequest(
    val createdById: String,
    val pendingFoodId: Int,
    val rejectionReason: String?
)

@Serializable
data class AddBarcodeRequest(
    val barcode: String,
    val edibleId: Int,
    val edibleType: String
)