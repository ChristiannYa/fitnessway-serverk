package com.example.repository.foods.pending

import com.example.domain.*
import com.example.mapping.PendingFoodDao
import com.example.mapping.UsersTable
import com.example.mapping.toDomain
import com.example.repository.PF
import com.example.repository.PFN
import com.example.repository.foods.queryNutrientsForFood
import com.example.utils.suspendTransaction
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.deleteWhere
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.*

class PendingFoodRepository : IPendingFoodRepository {
    override suspend fun findById(id: Int, userId: UUID): PendingFood? = suspendTransaction {
        val pendingFoodDao = PendingFoodDao.findById(id)
            ?: return@suspendTransaction null

        val nutrients = queryNutrientsForFood(PFN, pendingFoodDao.id.value, userId)

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
                this.createdBy = EntityID(foodToCreate.author, UsersTable)
                this.createdAt = Instant.now()
            }

            // Insert nutrients
            PFN.batchInsert(foodToCreate.foodInformation.nutrients) { d ->
                this[PFN.foodId] = pendingFoodDao.id
                this[PFN.nutrientId] = d.nutrientId
                this[PFN.amount] = d.amount.toBigDecimal()
            }

            val nutrients = queryNutrientsForFood(PFN, pendingFoodDao.id.value, foodToCreate.author)

            pendingFoodDao.toDomain(nutrients)
        }
    }

    override suspend fun updateStatus(pendingFoodReview: PendingFoodReview): PendingFood? = suspendTransaction {
        pendingFoodReview.let {
            val pendingFoodDao = PendingFoodDao.findById(it.pendingFoodId)
                ?: return@suspendTransaction null

            pendingFoodDao.apply {
                status = it.approvalStatus
                reviewedBy = EntityID(it.reviewerPrincipal.id, UsersTable)
                reviewedAt = Instant.now()
                it.rejectionReason?.let { reason -> rejectionReason = reason }
            }

            val nutrients = queryNutrientsForFood(PFN, pendingFoodDao.id.value, it.reviewerPrincipal.id)

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

    override suspend fun isDuplicate(food: FoodInformation<NutrientIdWithAmount>): Boolean = suspendTransaction {
        val pendingFoodBaseDaos = PendingFoodDao.find {
            (PF.name eq food.base.name) and
            (PF.brand eq food.base.brand.toString()) and
            (PF.amountPerServing eq food.base.amountPerServing.toBigDecimal()) and
            (PF.servingUnit eq food.base.servingUnit)
        }

        pendingFoodBaseDaos.any { foodBaseDao ->
            val foodBaseDaoNutrients = PFN
                .select(PFN.nutrientId, PFN.amount)
                .where { PFN.foodId eq foodBaseDao.id }
                .map { row ->
                    NutrientIdWithAmount(
                        nutrientId = row[PFN.nutrientId].value,
                        amount = row[PFN.amount].toDouble()
                    )
                }

            food.nutrients == foodBaseDaoNutrients
        }
    }

    override suspend fun delete(pendingFoodId: Int): Unit = suspendTransaction {
        PF.deleteWhere { id eq pendingFoodId }
    }
}