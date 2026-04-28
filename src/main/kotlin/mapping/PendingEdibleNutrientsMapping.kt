package com.example.mapping

import org.jetbrains.exposed.sql.Table

object UPEN : Table("user_pending_edible_nutrients"), EdibleNutrientTable {
    override val edibleId = reference("pending_edible_id", PE)
    override val nutrientId = reference("nutrient_id", N)
    override val amount = decimal("amount", 12, 4)

    override val primaryKey = PrimaryKey(edibleId, nutrientId)
}
