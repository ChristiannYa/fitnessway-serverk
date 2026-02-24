package com.example.mapping

import org.jetbrains.exposed.sql.Table

object AppFoodNutrientsTable : Table("app_food_nutrients"), FoodNutrientTable {
    override val foodId = reference("app_food_id", AppFoodsTable)
    override val nutrientId = reference("nutrient_id", NutrientsTable)
    override val amount = decimal("amount", 12, 4)

    override val primaryKey = PrimaryKey(foodId, nutrientId)
}