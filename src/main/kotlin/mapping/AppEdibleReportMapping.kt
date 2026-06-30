package com.example.mapping

import com.example.domain.AppEdibleReport
import com.example.utils.toEnum
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.timestampWithTimeZone
import java.time.OffsetDateTime
import java.time.ZoneOffset
import kotlin.time.toKotlinInstant

object AER : IntIdTable("app_edible_reports") {
    val edibleId = reference("edible_id", AE)
    val reportedBy = reference("reported_by", U).nullable()
    val notes = text("notes").nullable()
    val status = text("status").default(AppEdibleReport.Status.PENDING.toString().lowercase())
    val createdAt = timestampWithTimeZone("created_at").clientDefault { OffsetDateTime.now(ZoneOffset.UTC) }
    val reviewedAt = timestampWithTimeZone("reviewed_at").nullable()
    val reviewedBy = reference("reviewed_by", U).nullable()
}

class AERDao(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<AERDao>(AER)

    var edibleId by AER.edibleId
    var reportedBy by AER.reportedBy
    var notes by AER.notes
    var status by AER.status
    var createdAt by AER.createdAt
    var reviewedAt by AER.reviewedAt
    var reviewedBy by AER.reviewedBy
}

fun AERDao.toDto(reasons: List<AppEdibleReport.Reason>) = AppEdibleReport(
    id = this.id.value,
    edibleId = this.edibleId.value,
    reportedBy = this.reportedBy?.value,
    reasons = reasons,
    notes = this.notes,
    status = this.status.toEnum(),
    createdAt = this.createdAt.toInstant().toKotlinInstant(),
    reviewedAt = this.reviewedAt?.toInstant()?.toKotlinInstant(),
    reviewedBy = this.reviewedBy?.value,
)
