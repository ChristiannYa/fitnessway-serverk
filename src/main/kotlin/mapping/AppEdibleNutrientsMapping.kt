package com.example.mapping

import org.jetbrains.exposed.sql.Table

object AEN : Table("app_edible_nutrients"), EdibleNutrientTable {
    override val edibleId = reference("app_edible_id", AE)
    override val nutrientId = reference("nutrient_id", N)
    override val amount = decimal("amount", 12, 4)

    override val primaryKey = PrimaryKey(edibleId, nutrientId)
}