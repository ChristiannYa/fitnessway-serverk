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

object PF : IntIdTable("user_pending_foods") {
    val name = varchar("name", 50)
    val brand = varchar("brand", 50)
    val amountPerServing = decimal("amount_per_serving", 12, 4)
    val servingUnit = pgEnum<ServingUnit>("serving_unit", "serving_unit")
    val status = pgEnum<PendingFoodStatus>("status", "app_food_pending_status")
    val createdBy = reference("created_by", U).nullable()
    val reviewedBy = reference("reviewed_by", U).nullable()
    val reviewedAt = timestamp("reviewed_at").nullable()
    val createdAt = timestamp("created_at")
    val rejectionReason = text("rejection_reason").nullable()
}

class PFDao(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<PFDao>(PF)

    var name by PF.name
    var brand by PF.brand
    var amountPerServing by PF.amountPerServing
    var servingUnit by PF.servingUnit
    var status by PF.status
    var createdBy by PF.createdBy
    var reviewedBy by PF.reviewedBy
    var reviewedAt by PF.reviewedAt
    var createdAt by PF.createdAt
    var rejectionReason by PF.rejectionReason
}

fun PFDao.toDto(nutrients: NutrientsByType<NutrientDataAmount>) = PendingFood(
    id = this.id.value,
    information = FoodInformationDto(
        base = EdibleBase(
            name = this.name,
            brand = this.brand,
            amountPerServing = this.amountPerServing.toDouble(),
            servingUnit = this.servingUnit
        ),
        nutrients = nutrients
    ),
    createdBy = this.createdBy?.value,
    status = this.status,
    reviewedBy = this.reviewedBy?.value,
    reviewedAt = this.reviewedAt?.toKotlinInstant(),
    createdAt = this.createdAt.toKotlinInstant(),
    rejectionReason = this.rejectionReason
)