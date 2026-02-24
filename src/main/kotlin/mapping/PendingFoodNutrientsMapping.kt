package com.example.mapping

import org.jetbrains.exposed.sql.Table

object PendingFoodNutrientsTable : Table("user_pending_food_nutrients"), FoodNutrientTable {
    override val foodId = reference("pending_food_id", PendingFoodsTable)
    override val nutrientId = reference("nutrient_id", NutrientsTable)
    override val amount = decimal("amount", 12, 4)

    override val primaryKey = PrimaryKey(foodId, nutrientId)
}
