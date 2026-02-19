package com.example.mapping

import org.jetbrains.exposed.sql.Table

object UserNutrientPreferencesTable : Table("user_nutrient_preferences") {
    val userId = reference("user_id", UsersTable)
    val nutrientId = reference("nutrient_id", NutrientsTable)
    val goal = decimal("goal", 12, 4).nullable()
    val hexColor = varchar("hex_color", 7).nullable()

    override val primaryKey = PrimaryKey(userId, nutrientId)
}