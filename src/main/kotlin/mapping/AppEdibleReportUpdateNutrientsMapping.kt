package com.example.mapping

import org.jetbrains.exposed.sql.Table

object AERUN : Table("app_edible_report_update_nutrients"), EdibleNutrientTable {
    override val sourceId = reference("report_id", AERU)
    override val nutrientId = reference("nutrient_id", N)
    override val amount = decimal("amount", 12, 4)

    override val primaryKey = PrimaryKey(sourceId, nutrientId)
}