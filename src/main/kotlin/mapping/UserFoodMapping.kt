package com.example.mapping

import com.example.domain.FoodBase
import com.example.domain.NutrientInFood
import com.example.domain.ServingUnit
import com.example.domain.UserFood
import com.example.dto.FoodInformationDto
import com.example.mappers.toType
import com.example.utils.pgEnum
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.timestamp
import kotlin.time.toKotlinInstant

object UF : IntIdTable("user_foods") {
    val userId = reference("user_id", U)
    val name = varchar("name", 50)
    val brand = varchar("brand", 50).nullable()
    val amountPerServing = decimal("amount_per_serving", 12, 4)
    val servingUnit = pgEnum<ServingUnit>("serving_unit", "serving_unit")
    val isFavorite = bool("is_favorite")
    val lastLoggedAt = timestamp("last_logged_at").nullable()
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
}

class UFDao(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<UFDao>(UF)

    var userId by UF.userId
    var name by UF.name
    var brand by UF.brand
    var amountPerServing by UF.amountPerServing
    var servingUnit by UF.servingUnit
    var isFavorite by UF.isFavorite
    var lastLoggedAt by UF.lastLoggedAt
    var createdAt by UF.createdAt
    var updatedAt by UF.updatedAt
}

fun UFDao.toBase() = FoodBase(
    name = this.name,
    brand = this.brand,
    amountPerServing = this.amountPerServing.toDouble(),
    servingUnit = this.servingUnit
)

fun UFDao.toDto(nutrients: List<NutrientInFood>) = UserFood(
    id = this.id.value,
    information = FoodInformationDto(
        base = this.toBase(),
        nutrients = nutrients.toType(),
    ),
    isFavorite = this.isFavorite,
    lastLoggedAt = this.lastLoggedAt?.toKotlinInstant(),
    createdAt = this.createdAt.toKotlinInstant(),
    updatedAt = this.updatedAt.toKotlinInstant()
)