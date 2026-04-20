package com.example.mapping

import com.example.domain.FoodBase
import com.example.domain.ServingUnit
import com.example.domain.UserEdibleSnapshotStatus
import com.example.utils.pgEnum
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.timestamp

object UES : IntIdTable("user_edible_snapshots") {
    val originalEdibleId = integer("original_edible_id")
    val userId = reference("user_id", U)
    val name = varchar("name", 50)
    val brand = varchar("brand", 50).nullable()
    val amountPerServing = decimal("amount_per_serving", 12, 4)
    val servingUnit = pgEnum<ServingUnit>("serving_unit", "serving_unit")
    val snapshotStatus = pgEnum<UserEdibleSnapshotStatus>("snapshot_status", "user_edible_snapshot_status")
    val createdAt = timestamp("created_at")
}

class UFSDao(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<UFSDao>(UES)

    var originalEdibleId by UES.originalEdibleId
    var userId by UES.userId
    var name by UES.name
    var brand by UES.brand
    var amountPerServing by UES.amountPerServing
    var servingUnit by UES.servingUnit
    var snapshotStatus by UES.snapshotStatus
    var createdAt by UES.createdAt
}

fun UFSDao.toBase() = FoodBase(
    name = this.name,
    brand = this.brand,
    amountPerServing = this.amountPerServing.toDouble(),
    servingUnit = this.servingUnit
)