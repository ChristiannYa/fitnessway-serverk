package com.example.mapping

import com.example.domain.*
import com.example.utils.pgEnum
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.timestamp
import kotlin.time.toKotlinInstant

object PendingFoodsTable : IntIdTable("user_pending_foods") {
    val name = varchar("name", 50)
    val brand = varchar("brand", 50)
    val amountPerServing = decimal("amount_per_serving", 12, 4)
    val servingUnit = pgEnum<ServingUnit>("serving_unit", "serving_unit")
    val status = pgEnum<PendingFoodStatus>("status", "app_food_pending_status")
    val createdBy = reference("created_by", UsersTable)
    val reviewedBy = reference("reviewed_by", UsersTable).nullable()
    val reviewedAt = timestamp("reviewed_at").nullable()
    val createdAt = timestamp("created_at")
    val rejectionReason = text("rejection_reason").nullable()
}

class PendingFoodDao(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<PendingFoodDao>(PendingFoodsTable)

    var name by PendingFoodsTable.name
    var brand by PendingFoodsTable.brand
    var amountPerServing by PendingFoodsTable.amountPerServing
    var servingUnit by PendingFoodsTable.servingUnit
    var status by PendingFoodsTable.status
    var createdBy by PendingFoodsTable.createdBy
    var reviewedBy by PendingFoodsTable.reviewedBy
    var reviewedAt by PendingFoodsTable.reviewedAt
    var createdAt by PendingFoodsTable.createdAt
    var rejectionReason by PendingFoodsTable.rejectionReason
}

fun PendingFoodDao.toDomain(nutrients: List<NutrientInFood>) = PendingFood(
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
    createdBy = this.createdBy.value,
    status = this.status,
    reviewedBy = this.reviewedBy?.value,
    reviewedAt = this.reviewedAt?.toKotlinInstant(),
    createdAt = this.createdAt.toKotlinInstant(),
    rejectionReason = this.rejectionReason
)