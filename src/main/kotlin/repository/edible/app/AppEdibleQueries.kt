package com.example.repository.edible.app

import com.example.mapping.AE
import com.example.mapping.AEB
import org.jetbrains.exposed.sql.ColumnSet
import org.jetbrains.exposed.sql.JoinType

fun ColumnSet.barcodesJoin() = this
    .join(
        joinType = JoinType.INNER,
        otherTable = AEB,
        onColumn = AE.id,
        otherColumn = AEB.edibleId
    )