package com.example.domain

import kotlinx.serialization.Serializable
import java.math.BigDecimal
import java.util.*

@Serializable
enum class NutrientType {
    BASIC,
    VITAMIN,
    MINERAL,
}

/**
 * Represents a nutrient entry, either as a full [NutrientDataAmount] or
 * as a [NutrientIdWithAmount].
 */
sealed class NutrientEntry

/**
 * Represents nutrient information that can be grouped a property
 */
interface NutrientGroupable {
    val byId: Int
    val byType: NutrientType
    val bySortOrder: Int
}

typealias NutrientIntakes = NutrientsByType<NutrientDataAmount>

@Serializable
data class NutrientsByType<N : NutrientGroupable>(
    val basic: List<N>,
    val vitamin: List<N>,
    val mineral: List<N>
)

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
data class NutrientConfiguration(
    // val nutrientId: Int,
    val parentId: Int? = null,
    val sortOrder: Int
)

@Serializable
data class NutrientData(
    val base: NutrientBase,
    val preferences: NutrientPreferences,
    val configuration: NutrientConfiguration
) : NutrientGroupable {

    override val byId: Int
        get() = this.base.id

    override val byType: NutrientType
        get() = this.base.type

    override val bySortOrder: Int
        get() = this.configuration.sortOrder
}

@Serializable
data class NutrientDataAmount(
    val data: NutrientData,
    val amount: Double
) : NutrientEntry(), NutrientGroupable {

    override val byId: Int
        get() = this.data.base.id

    override val byType: NutrientType
        get() = this.data.base.type

    override val bySortOrder: Int
        get() = this.data.configuration.sortOrder
}

@Serializable
data class NutrientIdWithAmount(
    val id: Int,
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
    val source: LogSource
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