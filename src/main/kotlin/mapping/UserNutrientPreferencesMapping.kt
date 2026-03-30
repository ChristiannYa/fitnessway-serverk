package com.example.mapping

import com.example.domain.NutrientPreferences
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table

object UNP : Table("user_nutrient_preferences") {
    val userId = reference("user_id", U)
    val nutrientId = reference("nutrient_id", N)
    val goal = decimal("goal", 12, 4).nullable()
    val hexColor = varchar("hex_color", 7).nullable()

    override val primaryKey = PrimaryKey(userId, nutrientId)
}

fun UNP.toNutrientPreferences(row: ResultRow) = NutrientPreferences(
    hexColor = row.getOrNull(this.hexColor),
    goal = row.getOrNull(UNP.goal)?.toDouble()
)