package com.example.mapping

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestampWithTimeZone
import java.time.OffsetDateTime

object AEB : Table("app_edible_barcodes") {
    val barcode = varchar("barcode", 20)
    val edibleId = reference("edible_id", AE)
    val createdAt = timestampWithTimeZone("created_at").clientDefault { OffsetDateTime.now() }

    override val primaryKey = PrimaryKey(barcode)
}
