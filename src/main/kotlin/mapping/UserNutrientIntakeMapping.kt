package com.example.mapping

import org.jetbrains.exposed.sql.Table

object UNI : Table("user_nutrient_intake") {
    val userId = reference("user_id", U)
    val foodLogId = reference("food_log_id", UFL)
    val nutrientId = reference("nutrient_id", N)
    val intakeAmount = decimal("amount", 12, 4)
}