package com.example.repository.nutrient

import com.example.mapping.N
import com.example.mapping.NC
import com.example.mapping.UNP
import org.jetbrains.exposed.sql.ColumnSet
import org.jetbrains.exposed.sql.JoinType
import java.util.*

fun ColumnSet.nutrientPrefsJoin(userId: UUID) = this
    .join(
        joinType = JoinType.LEFT,
        otherTable = UNP,
        onColumn = N.id,
        otherColumn = UNP.nutrientId,
        additionalConstraint = { UNP.userId eq userId }
    )

fun ColumnSet.nutrientConfigJoin() = this
    .join(
        joinType = JoinType.INNER,
        otherTable = NC,
        onColumn = N.id,
        otherColumn = NC.nutrientId
    )

fun ColumnSet.nutrientDataJoins(userId: UUID) = this
    .nutrientPrefsJoin(userId)
    .nutrientConfigJoin()
