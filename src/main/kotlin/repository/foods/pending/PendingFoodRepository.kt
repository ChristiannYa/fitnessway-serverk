package com.example.repository.foods.pending

import com.example.domain.*
import com.example.exception.PendingFoodNotFoundException
import com.example.mapping.AppFoodDao
import com.example.mapping.PendingFoodDao
import com.example.mapping.UsersTable
import com.example.mapping.toDomain
import com.example.repository.*
import com.example.utils.suspendTransaction
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.*

class PendingFoodRepository : IPendingFoodRepository {
    override suspend fun findById(id: Int, userId: UUID): PendingFood? = suspendTransaction {
        val pendingFoodDao = PendingFoodDao.findById(id)
            ?: return@suspendTransaction null

        val nutrients = queryNutrientsForPendingFood(id, userId)

        pendingFoodDao.toDomain(nutrients)
    }

    override suspend fun create(foodToCreate: PendingFoodCreate): PendingFood = suspendTransaction {
        foodToCreate.let {
            val foodInformation = it.foodInformation.base

            // Insert into user_pending_foods
            val pendingFoodDao = PendingFoodDao.new {
                this.name = foodInformation.name
                this.brand = foodInformation.brand.toString()
                this.amountPerServing = foodInformation.amountPerServing.toBigDecimal()
                this.servingUnit = foodInformation.servingUnit
                this.status = PendingFoodStatus.PENDING
                this.createdBy = EntityID(foodToCreate.submittedBy, UsersTable)
                this.createdAt = Instant.now()
            }

            // Insert nutrients
            PFN.batchInsert(foodToCreate.foodInformation.nutrients) { d ->
                this[PFN.pendingFoodId] = pendingFoodDao.id
                this[PFN.nutrientId] = d.nutrientId
                this[PFN.amount] = d.amount.toBigDecimal()
            }

            val nutrients = queryNutrientsForPendingFood(pendingFoodDao.id.value, foodToCreate.submittedBy)

            pendingFoodDao.toDomain(nutrients)
        }
    }

    override suspend fun updateStatus(pendingFoodReview: PendingFoodReview): PendingFood? = suspendTransaction {
        pendingFoodReview.let {
            val pendingFoodDao = PendingFoodDao.findById(it.pendingFoodId)
                ?: return@suspendTransaction null

            pendingFoodDao.apply {
                status = it.getApprovalStatus()
                reviewedBy = EntityID(it.reviewerId, UsersTable)
                reviewedAt = Instant.now()
                it.rejectionReason?.let { reason -> rejectionReason = reason }
            }

            val nutrients = queryNutrientsForPendingFood(pendingFoodDao.id.value, pendingFoodDao.createdBy.value)

            pendingFoodDao.toDomain(nutrients)
        }
    }

    override suspend fun countUserSubmissionsOfDay(userId: UUID, date: LocalDate): Int = suspendTransaction {
        val startOfDay = date.atStartOfDay().toInstant(ZoneOffset.UTC)
        val endOfDay = date.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC)

        PendingFoodDao.find {
            (PF.createdBy eq userId) and
            (PF.createdAt greaterEq startOfDay) and
            (PF.createdAt less endOfDay)
        }.count().toInt()
    }

    override suspend fun moveToAppFoods(pendingFoodId: Int): Int = suspendTransaction {
        val pendingFoodDao = PendingFoodDao.findById(pendingFoodId)
            ?: throw PendingFoodNotFoundException(
                "pending food with id $pendingFoodId not found attempting to move pending food to app's schema"
            )

        val appFoodDao = AppFoodDao.new {
            this.name = pendingFoodDao.name
            this.brand = pendingFoodDao.brand
            this.amountPerServing = pendingFoodDao.amountPerServing
            this.servingUnit = pendingFoodDao.servingUnit
            this.createdBy = pendingFoodDao.createdBy
        }

        val nutrients = queryNutrientsForPendingFood(pendingFoodDao.id.value, pendingFoodDao.createdBy.value)
            .map { it.nutrientData.base.id to it.amount }

        AFN.batchInsert(nutrients) { (nutrientId, amount) ->
            this[AFN.appFoodId] = appFoodDao.id.value
            this[AFN.nutrientId] = nutrientId
            this[AFN.amount] = amount.toBigDecimal()
        }

        appFoodDao.id.value
    }

    override suspend fun isDuplicateSubmission(userId: UUID, foodBase: FoodBase): Boolean = suspendTransaction {
        PendingFoodDao.find {
            (PF.createdBy eq userId) and
            (PF.name eq foodBase.name) and
            (PF.brand eq foodBase.brand.toString()) and
            (PF.amountPerServing eq foodBase.amountPerServing.toBigDecimal()) and
            (PF.servingUnit eq foodBase.servingUnit)
        }.count() > 0
    }

    private fun queryNutrientsForPendingFood(
        pendingFoodId: Int,
        userId: UUID
    ): List<NutrientInFood> {
        return (PFN innerJoin N)
            // LEFT JOIN user_nutrient_preferences
            // ON nutrients.id = user_nutrient_preferences.nutrient_id
            // AND user_nutrient_preferences.user_id = ?
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

    override suspend fun delete(pendingFoodId: Int): Unit = suspendTransaction {
        PF.deleteWhere { id eq pendingFoodId }
    }
}