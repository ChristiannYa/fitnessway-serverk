package com.example.mapping

import com.example.domain.ServingUnit
import com.example.domain.UserFoodSnapshotStatus
import com.example.utils.pgEnum
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.timestamp

object UFS : IntIdTable("user_food_snapshots") {
    val originalFoodId = integer("original_food_id")
    val userId = reference("user_id", U)
    val name = varchar("name", 50)
    val brand = varchar("brand", 50).nullable()
    val amountPerServing = decimal("amount_per_serving", 12, 4)
    val servingUnit = pgEnum<ServingUnit>("serving_unit", "serving_unit")
    val foodStatus = pgEnum<UserFoodSnapshotStatus>("food_status", "food_status")
    val createdAt = timestamp("created_at")
}

class UFSDao(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<UFSDao>(UFS)

    var originalFoodId by UFS.originalFoodId
    var userId by UFS.userId
    var name by UFS.name
    var brand by UFS.brand
    var amountPerServing by UFS.amountPerServing
    var servingUnit by UFS.servingUnit
    var foodStatus by UFS.foodStatus
    var createdAt by UFS.createdAt
}