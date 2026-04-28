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

object PE : IntIdTable("user_pending_edibles") {
    val name = varchar("name", 50)
    val brand = varchar("brand", 50)
    val amountPerServing = decimal("amount_per_serving", 12, 4)
    val servingUnit = pgEnum<ServingUnit>("serving_unit", "serving_unit")
    val status = pgEnum<PendingFoodStatus>("status", "app_edible_pending_status")
    val edibleType = pgEnum<EdibleType>("edible_type", "edible_type")
    val createdBy = reference("created_by", U).nullable()
    val reviewedBy = reference("reviewed_by", U).nullable()
    val reviewedAt = timestamp("reviewed_at").nullable()
    val createdAt = timestamp("created_at")
    val rejectionReason = text("rejection_reason").nullable()
}

class PEDao(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<PEDao>(PE)

    var name by PE.name
    var brand by PE.brand
    var amountPerServing by PE.amountPerServing
    var servingUnit by PE.servingUnit
    var status by PE.status
    var edibleType by PE.edibleType
    var createdBy by PE.createdBy
    var reviewedBy by PE.reviewedBy
    var reviewedAt by PE.reviewedAt
    var createdAt by PE.createdAt
    var rejectionReason by PE.rejectionReason
}

fun PEDao.toDto(nutrients: NutrientsByType<NutrientDataAmount>) = PendingFood(
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
    edibleType = this.edibleType,
    createdBy = this.createdBy?.value,
    status = this.status,
    reviewedBy = this.reviewedBy?.value,
    reviewedAt = this.reviewedAt?.toKotlinInstant(),
    createdAt = this.createdAt.toKotlinInstant(),
    rejectionReason = this.rejectionReason
)