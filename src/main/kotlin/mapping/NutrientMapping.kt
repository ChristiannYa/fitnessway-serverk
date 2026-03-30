package com.example.mapping

import com.example.domain.NutrientBase
import com.example.domain.NutrientType
import com.example.domain.ServingUnit
import com.example.utils.pgEnum
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ResultRow

object N : IntIdTable("nutrients") {
    val name = varchar("name", 50)
    val symbol = varchar("symbol", 4).nullable()
    val unit = pgEnum<ServingUnit>("unit", "serving_unit")
    val type = pgEnum<NutrientType>("type", "nutrient_type")
    val isPremium = bool("is_premium")
}

class NDao(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<NDao>(N)

    var name by N.name
    var symbol by N.symbol
    var unit by N.unit
    var type by N.type
    var isPremium by N.isPremium
}

fun N.toBase(row: ResultRow) = NutrientBase(
    id = row[this.id].value,
    name = row[this.name],
    unit = row[this.unit],
    type = row[this.type],
    symbol = row[this.symbol],
    isPremium = row[this.isPremium]
)