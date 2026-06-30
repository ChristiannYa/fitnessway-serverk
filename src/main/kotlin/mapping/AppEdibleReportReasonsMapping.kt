package com.example.mapping

import org.jetbrains.exposed.sql.Table

object AERR : Table("app_edible_report_reasons") {
    val reportId = reference("report_id", AER)
    val reason = text("reason")
    override val primaryKey = PrimaryKey(reportId, reason)
}
