package com.example.mapping

import com.example.domain.FoodLog
import com.example.domain.LogCategory
import com.example.domain.LogSource
import com.example.domain.UserEdibleSnapshotStatus
import com.example.dto.FoodInformationDto
import com.example.utils.pgEnum
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.timestampWithTimeZone
import kotlin.time.toKotlinInstant

object UEL : IntIdTable("user_edible_logs") {
    val userId = reference("user_id", U)
    val edibleId = integer("edible_id").nullable()
    val edibleSnapshotId = reference("edible_snapshot_id", UES).nullable()
    val servings = decimal("servings", 12, 4)
    val category = pgEnum<LogCategory>("category", "log_category")
    val time = timestampWithTimeZone("time")
    val loggedAt = timestampWithTimeZone("logged_at")
    val logSource = pgEnum<LogSource>("source", "log_source")
}

class UELDao(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<UELDao>(UEL)

    var userId by UEL.userId
    var edibleId by UEL.edibleId
    var edibleSnapshotId by UEL.edibleSnapshotId
    var servings by UEL.servings
    var category by UEL.category
    var time by UEL.time
    var loggedAt by UEL.loggedAt
    var logSource by UEL.logSource
}

fun UELDao.toDto(
    userEdibleSnapshotStatus: UserEdibleSnapshotStatus?,
    foodInformationDto: FoodInformationDto
) = FoodLog(
    id = this.id.value,
    category = this.category,
    time = this.time.toInstant().toKotlinInstant(),
    loggedAt = this.loggedAt.toInstant().toKotlinInstant(),
    servings = this.servings.toDouble(),
    userEdibleSnapshotStatus = userEdibleSnapshotStatus,
    userEdibleSnapshotId = this.edibleSnapshotId?.value,
    source = this.logSource,
    edibleId = this.edibleId,
    edibleInformation = foodInformationDto
)