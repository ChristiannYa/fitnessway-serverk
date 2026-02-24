package com.example.mapping

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.Column
import java.math.BigDecimal

interface FoodNutrientTable {
    val foodId: Column<EntityID<Int>>
    val nutrientId: Column<EntityID<Int>>
    val amount: Column<BigDecimal>
}