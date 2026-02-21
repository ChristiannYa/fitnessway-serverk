package com.example.mapping

import com.example.domain.*
import com.example.utils.pgEnum
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.timestamp
import java.time.Instant
import kotlin.time.toKotlinInstant

object AppFoodsTable : IntIdTable("app_foods") {
    val name = varchar("name", 50)
    val brand = varchar("brand", 50)
    val amountPerServing = decimal("amount_per_serving", 12, 4)
    val servingUnit = pgEnum<ServingUnit>("serving_unit", "serving_unit")
    val createdBy = reference("created_by", UsersTable).nullable()
    val createdAt = timestamp("created_at").clientDefault { Instant.now() }
    val updatedAt = timestamp("updated_at").clientDefault { Instant.now() }
}

class AppFoodDao(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<AppFoodDao>(AppFoodsTable)

    var name by AppFoodsTable.name
    var brand by AppFoodsTable.brand
    var amountPerServing by AppFoodsTable.amountPerServing
    var servingUnit by AppFoodsTable.servingUnit
    var createdBy by AppFoodsTable.createdBy
    var createdAt by AppFoodsTable.createdAt
    var updatedAt by AppFoodsTable.updatedAt
}

fun AppFoodDao.toDomain(nutrients: List<NutrientInFood>) = AppFood(
    id = this.id.value,
    information = FoodInformation(
        base = FoodBase(
            name = this.name,
            brand = this.brand,
            amountPerServing = this.amountPerServing.toDouble(),
            servingUnit = this.servingUnit
        ),
        nutrients = nutrients
    ),
    createdBy = this.createdBy?.value,
    createdAt = this.createdAt.toKotlinInstant(),
    updatedAt = this.updatedAt.toKotlinInstant()
)