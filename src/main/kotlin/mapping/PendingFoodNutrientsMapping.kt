package com.example.mapping

import org.jetbrains.exposed.sql.Table

object UPFN : Table("user_pending_food_nutrients"), FoodNutrientTable {
    override val foodId = reference("pending_food_id", PF)
    override val nutrientId = reference("nutrient_id", N)
    override val amount = decimal("amount", 12, 4)

    override val primaryKey = PrimaryKey(foodId, nutrientId)
}
