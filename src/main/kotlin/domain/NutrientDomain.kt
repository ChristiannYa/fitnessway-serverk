package com.example.domain

import kotlinx.serialization.Serializable

@Serializable
enum class NutrientType {
    BASIC,
    VITAMIN,
    MINERAL,
}

/**
 * Represents the type of the nutrient object that will be used on a
 * food object
 */
sealed interface NutrientGeneric

@Serializable
data class NutrientBase(
    val id: Int,
    val name: String,
    val unit: ServingUnit,
    val type: NutrientType,
    val symbol: String? = null,
    val isPremium: Boolean
)

@Serializable
data class NutrientPreferences(
    val hexColor: String? = null,
    val goal: Double? = null
)

@Serializable
data class NutrientData(
    val base: NutrientBase,
    val preferences: NutrientPreferences
)

@Serializable
data class NutrientInFood(
    val nutrientData: NutrientData,
    val amount: Double
) : NutrientGeneric

@Serializable
data class NutrientIdWithAmount(
    val nutrientId: Int,
    val amount: Double
) : NutrientGeneric