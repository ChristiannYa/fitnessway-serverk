package com.example.mapping

import com.example.domain.*
import com.example.dto.FoodInformationDto
import com.example.utils.pgEnum
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.timestamp
import kotlin.time.toKotlinInstant

object UE : IntIdTable("user_edibles") {
    val userId = reference("user_id", U)
    val name = varchar("name", 50)
    val brand = varchar("brand", 50).nullable()
    val amountPerServing = decimal("amount_per_serving", 12, 4)
    val servingUnit = pgEnum<ServingUnit>("serving_unit", "serving_unit")
    val edibleType = pgEnum<EdibleType>("edible_type", "edible_type")
    val lastLoggedAt = timestamp("last_logged_at").nullable()
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at").nullable()
}

class UEDao(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<UEDao>(UE)

    var userId by UE.userId
    var name by UE.name
    var brand by UE.brand
    var amountPerServing by UE.amountPerServing
    var servingUnit by UE.servingUnit
    var edibleType by UE.edibleType
    var lastLoggedAt by UE.lastLoggedAt
    var createdAt by UE.createdAt
    var updatedAt by UE.updatedAt
}

fun UEDao.toBase() = EdibleBase(
    name = this.name,
    brand = this.brand,
    amountPerServing = this.amountPerServing.toDouble(),
    servingUnit = this.servingUnit
)

fun UEDao.toDto(nutrients: NutrientsByType<NutrientDataAmount>) = UserEdible(
    id = this.id.value,
    information = FoodInformationDto(
        base = this.toBase(),
        nutrients = nutrients,
    ),
    lastLoggedAt = this.lastLoggedAt?.toKotlinInstant(),
    createdAt = this.createdAt.toKotlinInstant(),
    updatedAt = this.updatedAt?.toKotlinInstant()
)