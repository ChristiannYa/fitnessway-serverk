package com.example.mapping

import com.example.domain.NutrientType
import com.example.domain.ServingUnit
import com.example.utils.pgEnum
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

object NutrientsTable : IntIdTable("nutrients") {
    val name = varchar("name", 50)
    val symbol = varchar("symbol", 4).nullable()
    val unit = pgEnum<ServingUnit>("unit", "serving_unit")
    val type = pgEnum<NutrientType>("type", "nutrient_type")
    val isPremium = bool("is_premium")
}

class NutrientDao(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<NutrientDao>(NutrientsTable)

    var name by NutrientsTable.name
    var symbol by NutrientsTable.symbol
    var unit by NutrientsTable.unit
    var type by NutrientsTable.type
    var isPremium by NutrientsTable.isPremium
}