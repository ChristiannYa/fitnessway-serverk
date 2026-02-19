package com.example.mapping

import org.jetbrains.exposed.sql.Table

object PendingFoodNutrientsTable : Table("user_pending_food_nutrients") {
    val pendingFoodId = reference("pending_food_id", PendingFoodsTable)
    val nutrientId = reference("nutrient_id", NutrientsTable)
    val amount = decimal("amount", 12, 4)

    override val primaryKey = PrimaryKey(pendingFoodId, nutrientId)
}
