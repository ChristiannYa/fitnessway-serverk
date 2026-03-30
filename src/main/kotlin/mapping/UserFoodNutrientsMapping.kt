package com.example.mapping

import org.jetbrains.exposed.sql.Table

object UFN : Table("user_food_nutrients"), FoodNutrientTable {
    override val foodId = reference("user_food_id", UF)
    override val nutrientId = reference("nutrient_id", N)
    override val amount = decimal("amount", 12, 4)

    override val primaryKey = PrimaryKey(foodId, nutrientId)
}