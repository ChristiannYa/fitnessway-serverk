package com.example.repository.nutrient

import com.example.domain.*
import com.example.mapping.N
import com.example.mapping.NC
import com.example.mapping.UNP
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ResultRow
import java.math.BigDecimal

fun ResultRow.toNutrientData() = NutrientData(
    base = NutrientBase(
        id = this[N.id].value,
        name = this[N.name],
        unit = this[N.unit],
        type = this[N.type],
        symbol = this[N.symbol],
        isPremium = this[N.isPremium]
    ),
    preferences = NutrientPreferences(
        hexColor = this.getOrNull(UNP.hexColor),
        goal = this.getOrNull(UNP.goal)?.toDouble()
    ),
    configuration = NutrientConfiguration(
        parentId = this[NC.parentId]?.value,
        sortOrder = this[NC.sortOrder]
    )
)

fun ResultRow.toNutrientDataAmount(amount: Column<BigDecimal>) = NutrientDataAmount(
    data = toNutrientData(),
    amount = this[amount].toDouble()
)