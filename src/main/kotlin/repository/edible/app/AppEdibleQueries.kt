package com.example.repository.edible.app

import com.example.mapping.AE
import com.example.mapping.AEB
import com.example.mapping.AER
import com.example.mapping.AERR
import org.jetbrains.exposed.sql.ColumnSet
import org.jetbrains.exposed.sql.JoinType

fun ColumnSet.barcodesJoin() = this
    .join(
        joinType = JoinType.INNER,
        otherTable = AEB,
        onColumn = AE.id,
        otherColumn = AEB.edibleId
    )

fun ColumnSet.joinReports() = this
    .join(
        joinType = JoinType.INNER,
        otherTable = AER,
        onColumn = AE.id,
        otherColumn = AER.edibleId
    )
    .join(
        joinType = JoinType.INNER,
        otherTable = AERR,
        onColumn = AER.id,
        otherColumn = AERR.reportId
    )
