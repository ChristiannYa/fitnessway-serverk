package com.example.mapping

import com.example.domain.FoodLog
import com.example.domain.FoodLogCategory
import com.example.domain.FoodSource
import com.example.domain.UserFoodSnapshotStatus
import com.example.dto.FoodInformationDto
import com.example.utils.pgEnum
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.timestampWithTimeZone
import kotlin.time.toKotlinInstant

object UFL : IntIdTable("user_food_logs") {
    val userId = reference("user_id", U)
    val foodId = integer("food_id").nullable()
    val foodSnapshotId = reference("food_snapshot_id", UFS).nullable()
    val servings = decimal("servings", 12, 4)
    val category = pgEnum<FoodLogCategory>("category", "food_log_category")
    val time = timestampWithTimeZone("time")
    val loggedAt = timestampWithTimeZone("logged_at")
    val foodSource = pgEnum<FoodSource>("source", "food_source")
}

class UFLDao(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<UFLDao>(UFL)

    var userId by UFL.userId
    var foodId by UFL.foodId
    var foodSnapshotId by UFL.foodSnapshotId
    var servings by UFL.servings
    var category by UFL.category
    var time by UFL.time
    var loggedAt by UFL.loggedAt
    var foodSource by UFL.foodSource
}

fun UFLDao.toDto(
    userFoodSnapshotStatus: UserFoodSnapshotStatus?,
    foodId: Int?,
    foodInformationDto: FoodInformationDto
) = FoodLog(
    id = this.id.value,
    category = this.category,
    time = this.time.toInstant().toKotlinInstant(),
    loggedAt = this.loggedAt.toInstant().toKotlinInstant(),
    servings = this.servings.toDouble(),
    userFoodSnapshotStatus = userFoodSnapshotStatus,
    userFoodSnapshotId = this.foodSnapshotId?.value,
    source = this.foodSource,
    foodId = foodId,
    foodInformation = foodInformationDto
)