package com.example.mapping

import com.example.domain.*
import com.example.dto.FoodInformationDto
import com.example.utils.pgEnum
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.timestamp
import java.time.Instant
import kotlin.time.toKotlinInstant

object AE : IntIdTable("app_edibles") {
    val name = varchar("name", 50)
    val brand = varchar("brand", 50)
    val amountPerServing = decimal("amount_per_serving", 12, 4)
    val servingUnit = pgEnum<ServingUnit>("serving_unit", "serving_unit")
    val edibleType = pgEnum<EdibleType>("edible_type", "edible_type")
    val createdBy = reference("created_by", U).nullable()
    val createdAt = timestamp("created_at").clientDefault { Instant.now() }
    val updatedAt = timestamp("updated_at").clientDefault { Instant.now() }
}

class AEDao(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<AEDao>(AE)

    var name by AE.name
    var brand by AE.brand
    var amountPerServing by AE.amountPerServing
    var servingUnit by AE.servingUnit
    var edibleType by AE.edibleType
    var createdBy by AE.createdBy
    var createdAt by AE.createdAt
    var updatedAt by AE.updatedAt
}

fun AEDao.toBase() = EdibleBase(
    name = this.name,
    brand = this.brand,
    amountPerServing = this.amountPerServing.toDouble(),
    servingUnit = this.servingUnit
)

fun AEDao.toDto(nutrients: NutrientsByType<NutrientDataAmount>) = AppFood(
    id = this.id.value,
    information = FoodInformationDto(
        base = this.toBase(),
        nutrients = nutrients
    ),
    createdBy = this.createdBy?.value,
    createdAt = this.createdAt.toKotlinInstant(),
    updatedAt = this.updatedAt.toKotlinInstant()
)