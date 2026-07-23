package com.example.mapping

import com.example.domain.EdibleType
import com.example.domain.ServingUnit
import com.example.utils.pgEnum
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

object AERU : IntIdTable("app_edible_report_updates", "report_id") {
    val name = varchar("name", 50)
    val brand = varchar("brand", 50)
    val amountPerServing = decimal("amount_per_serving", 12, 4)
    val servingUnit = pgEnum<ServingUnit>("serving_unit", "serving_unit")
    val edibleType = pgEnum<EdibleType>("edible_type", "edible_type")
    val barcode = varchar("barcode", 20)
}

class AERUDao(id: EntityID<Int>) : EdibleDao(id) {
    companion object : IntEntityClass<AERUDao>(AERU)

    var name by AERU.name
    var brand by AERU.brand
    var amountPerServing by AERU.amountPerServing
    var servingUnit by AERU.servingUnit
    var edibleType by AERU.edibleType
    var barcode by AERU.barcode
}
