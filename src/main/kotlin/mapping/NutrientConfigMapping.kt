package com.example.mapping

import org.jetbrains.exposed.sql.Table

object NC : Table("nutrient_config") {
    val nutrientId = reference("nutrient_id", N)
    val parentId = reference("parent_id", N).nullable()
    val sortOrder = integer("sort_order")
}