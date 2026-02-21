package com.example.mapping

import org.jetbrains.exposed.sql.Table

object AppFoodNutrientsTable : Table("app_food_nutrients") {
    val appFoodId = reference("app_food_id", AppFoodsTable)
    val nutrientId = reference("nutrient_id", NutrientsTable)
    val amount = decimal("amount", 12, 4)

    override val primaryKey = PrimaryKey(appFoodId, nutrientId)
}