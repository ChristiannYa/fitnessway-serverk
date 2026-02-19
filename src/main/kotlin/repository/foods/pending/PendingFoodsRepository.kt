package com.example.repository.foods.pending

import com.example.domain.*
import com.example.mapping.*
import com.example.utils.suspendTransaction
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.selectAll
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.*

private typealias PF = PendingFoodsTable
private typealias PFN = PendingFoodNutrientsTable
private typealias N = NutrientsTable
private typealias NP = UserNutrientPreferencesTable

class PendingFoodsRepository : IPendingFoodsRepository {
    override suspend fun create(foodToCreate: PendingFoodCreate): PendingFood = suspendTransaction {
        val foodInformation = foodToCreate.information.base

        // Insert into user_pending_foods
        val pendingFoodDao = PendingFoodDao.new {
            this.name = foodInformation.name
            this.brand = foodInformation.brand.toString()
            this.amountPerServing = foodInformation.amountPerServing.toBigDecimal()
            this.servingUnit = foodInformation.servingUnit
            this.status = AppFoodPendingStatus.PENDING
            this.submittedBy = EntityID(foodToCreate.submittedBy, UsersTable)
            this.createdAt = Instant.now()
        }

        // Insert nutrients
        PFN.batchInsert(foodToCreate.information.nutrients) {
            this[PFN.pendingFoodId] = pendingFoodDao.id
            this[PFN.nutrientId] = it.nutrientId
            this[PFN.amount] = it.amount.toBigDecimal()
        }

        val nutrients = queryNutrientsForPendingFood(pendingFoodDao.id.value, foodToCreate.submittedBy)

        pendingFoodDao.toDomain(nutrients)
    }

    override suspend fun findById(id: Int, userId: UUID): PendingFood? = suspendTransaction {
        val pendingFoodDao = PendingFoodDao.findById(id)
            ?: return@suspendTransaction null

        val nutrients = queryNutrientsForPendingFood(id, userId)

        pendingFoodDao.toDomain(nutrients)
    }

    private fun queryNutrientsForPendingFood(
        pendingFoodId: Int,
        userId: UUID
    ): List<NutrientInFood> {
        return (PFN innerJoin N)
            .join(
                joinType = JoinType.LEFT,
                otherTable = NP,
                onColumn = N.id,
                otherColumn = NP.nutrientId,
                additionalConstraint = { NP.userId eq userId }
            )
            .selectAll()
            .where { PFN.pendingFoodId eq pendingFoodId }
            .map { row ->
                NutrientInFood(
                    nutrientData = NutrientData(
                        base = NutrientBase(
                            id = row[N.id].value,
                            name = row[N.name],
                            unit = row[N.unit],
                            type = row[N.type],
                            symbol = row[N.symbol],
                            isPremium = row[N.isPremium]
                        ),
                        preferences = NutrientPreferences(
                            hexColor = row.getOrNull(NP.hexColor),
                            goal = row.getOrNull(NP.goal)?.toDouble()
                        )
                    ),
                    amount = row[PFN.amount].toDouble()
                )
            }
    }

    override suspend fun countUserSubmissionsOfDay(userId: UUID, date: LocalDate): Int = suspendTransaction {
        val startOfDay = date.atStartOfDay().toInstant(ZoneOffset.UTC)
        val endOfDay = date.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC)

        PendingFoodDao.find {
            (PF.submittedBy eq userId) and
            (PF.createdAt greaterEq startOfDay) and
            (PF.createdAt less endOfDay)
        }.count().toInt()
    }

    override suspend fun isDuplicateSubmission(userId: UUID, foodBase: FoodBase): Boolean = suspendTransaction {
        PendingFoodDao.find {
            (PF.submittedBy eq userId) and
            (PF.name eq foodBase.name) and
            (PF.brand eq foodBase.brand.toString()) and
            (PF.amountPerServing eq foodBase.amountPerServing.toBigDecimal()) and
            (PF.servingUnit eq foodBase.servingUnit)
        }.count() > 0
    }
}