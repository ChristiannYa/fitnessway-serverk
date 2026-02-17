package com.example.domain

import kotlin.time.Instant

enum class FoodServingUnit {
    G, MG, MCG, ML, OZ, KCAL
}

interface FoodBase {
    val id: Int
    val name: String
    val brand: String? get() = null
    val amountPerServing: Double
    val servingUnit: FoodServingUnit
}

data class AppFood(
    val base: FoodBase,
    val createdBy: Instant,
    val createdAt: Instant,
    val updatedAt: Instant? = null
) : FoodBase by base