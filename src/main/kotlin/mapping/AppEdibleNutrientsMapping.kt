package com.example.mapping

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestampWithTimeZone
import java.time.OffsetDateTime

object AEN : Table("app_edible_nutrients"), EdibleNutrientTable {
    override val sourceId = reference("app_edible_id", AE)
    override val nutrientId = reference("nutrient_id", N)
    override val amount = decimal("amount", 12, 4)
    val createdAt = timestampWithTimeZone("created_at").clientDefault { OffsetDateTime.now() }
    val updatedAt = timestampWithTimeZone("updated_at")

    override val primaryKey = PrimaryKey(sourceId, nutrientId)
}