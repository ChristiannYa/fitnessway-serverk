package com.example.mapping

import org.jetbrains.exposed.sql.Table

object UEN : Table("user_edible_nutrients"), EdibleNutrientTable {
    override val edibleId = reference("user_edible_id", UE)
    override val nutrientId = reference("nutrient_id", N)
    override val amount = decimal("amount", 12, 4)

    override val primaryKey = PrimaryKey(edibleId, nutrientId)
}