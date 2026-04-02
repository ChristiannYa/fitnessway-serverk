package com.example.domain

import kotlinx.serialization.Serializable
import java.math.BigDecimal
import java.util.*

/**
 * Represents a nutrient entry, either as a full [NutrientInFood] or
 * as a [NutrientIdWithAmount].
 */
sealed class NutrientEntry

/**
 * Represents nutrient information that can be grouped by [NutrientType]
 */
interface NutrientGroupable {
    val nutrientType: NutrientType
}

@Serializable
data class NutrientsByType<N : NutrientGroupable>(
    val basic: List<N>,
    val vitamins: List<N>,
    val minerals: List<N>
)

@Serializable
enum class NutrientType {
    BASIC,
    VITAMIN,
    MINERAL,
}

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
) : NutrientGroupable {
    override val nutrientType: NutrientType
        get() = this.base.type
}

@Serializable
data class NutrientInFood(
    val nutrientData: NutrientData,
    val amount: Double
) : NutrientEntry(), NutrientGroupable {
    override val nutrientType: NutrientType
        get() = this.nutrientData.nutrientType
}

@Serializable
data class NutrientIdWithAmount(
    val nutrientId: Int,
    val amount: Double
) : NutrientEntry()

@Serializable
data class NutrientAmountWithColor(
    val amount: Double? = null,
    val color: String? = null
)

@Serializable
data class NutrientPreview(
    val calories: NutrientAmountWithColor = NutrientAmountWithColor(),
    val carbs: NutrientAmountWithColor = NutrientAmountWithColor(),
    val fats: NutrientAmountWithColor = NutrientAmountWithColor(),
    val protein: NutrientAmountWithColor = NutrientAmountWithColor()
)

/**
 * Holds data needed in order to set nutrient intakes from food data
 */
data class NutrientIntakesFromFood(
    val userId: UUID,
    val foodLogId: Int,
    val foodId: Int,
    val servings: Double,
    val source: FoodSource
)

/**
 * Holds data needed in order to set nutrient intakes based off of its
 * current nutrient intakes
 */
data class NutrientIntakesFromCurrent(
    val userId: UUID,
    val foodLogId: Int,
    val curIntakes: List<NutrientIntakeRow>,
    val curServings: Double,
    val newServings: Double
)

data class NutrientIntakeRow(
    val nutrientId: Int,
    val amount: BigDecimal
)