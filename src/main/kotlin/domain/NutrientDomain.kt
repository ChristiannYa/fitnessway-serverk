package com.example.domain

import kotlinx.serialization.Serializable

@Serializable
enum class NutrientType {
    BASIC,
    VITAMIN,
    MINERAL,
}

/**
 * Represents a nutrient entry, either as a full [NutrientInFood] or
 * as a [NutrientIdWithAmount].
 */
sealed class NutrientEntry

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
) : NutrientEntry()

@Serializable
data class NutrientIdWithAmount(
    val nutrientId: Int,
    val amount: Double
) : NutrientEntry()