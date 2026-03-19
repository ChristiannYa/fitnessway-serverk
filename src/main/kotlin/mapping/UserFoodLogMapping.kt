package com.example.mapping

import com.example.domain.FoodLogCategory
import com.example.domain.FoodSource
import com.example.utils.pgEnum
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.timestamp

object UFL : IntIdTable("user_food_logs") {
    val userId = reference("user_id", U)
    val foodId = reference("food_id", UF).nullable()
    val foodSnapshotId = reference("food_snapshot_id", UFS).nullable()
    val servings = decimal("servings", 12, 4)
    val category = pgEnum<FoodLogCategory>("category", "food_log_category")
    val time = timestamp("time")
    val loggedAt = timestamp("logged_at")
    val foodSource = pgEnum<FoodSource>("source", "food_source")
}