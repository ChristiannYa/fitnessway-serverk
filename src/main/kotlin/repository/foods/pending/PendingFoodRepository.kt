package com.example.repository.foods.pending

import com.example.domain.*
import com.example.mappers.toCategoryGroups
import com.example.mappers.toClientFilter
import com.example.mapping.*
import com.example.repository.foods.queryNutrientsForFood
import com.example.repository.foods.queryNutrientsForFoods
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
        // @TODO: Should add by user AND constraint as well
        val pfDao = PFDao.findById(id)
            ?: return@suspendTransaction null

        val nutrients = queryNutrientsForFood(UPFN, pfDao.id.value, userId)
            .toClientFilter(isAppFood = true)
            .toCategoryGroups()

        pfDao.toDto(nutrients)
    }

    override suspend fun findPaginated(
        paginationCriteria: PaginationCriteria<PendingFoodsPaginationCriteria>
    ): Result<PaginationQuery<PendingFood>> = suspendTransaction {
        val criteria = paginationCriteria.data

        val query = run {
            val pfJoin: Join = PF.join(
                joinType = JoinType.INNER,
                otherTable = U,
                onColumn = PF.createdBy,
                otherColumn = U.id
            )

            when (val userScope = criteria.userScope) {
                is UserScope.Type ->
                    pfJoin
                        .selectAll()
                        .where { U.userType eq userScope.type }

                is UserScope.Id ->
                    pfJoin
                        .selectAll()
                        .where { PF.createdBy eq userScope.id }
            }
        }.apply {
            criteria.status?.let { andWhere { PF.status eq it } }
        }

        val pfDaos = query
            .limit(paginationCriteria.limit)
            .offset(paginationCriteria.offset)
            .map { row -> PFDao.wrapRow(row) }

        val foodIds = pfDaos.map { it.id.value }

        val foodNutrients = queryNutrientsForFoods(
            UPFN,
            foodIds,
            paginationCriteria.data.userId
        )

        val pfDaoMap = pfDaos.associateBy { it.id.value }
        val foods = foodIds.map { foodId ->
            val pfDao = pfDaoMap[foodId] ?: return@suspendTransaction Result.failure(
                IllegalStateException("pending food dao #${foodId} not found")
            )

            foodNutrients[foodId]?.let { nutrients ->
                pfDao.toDto(
                    nutrients
                        .toClientFilter(isAppFood = true)
                        .toCategoryGroups()
                )
            } ?: return@suspendTransaction Result.failure(
                IllegalStateException("pending food dao #${foodId}'s nutrients not found")
            )
        }

        Result.success(PaginationQuery(foods, query.count()))
    }

    override suspend fun create(foodToCreate: PendingFoodCreate): PendingFood = suspendTransaction {
        foodToCreate.let {
            val foodInformation = it.foodInformation.base

            // Insert into user_pending_foods
            val pfDao = PFDao.new {
                this.name = foodInformation.name
                this.brand = foodInformation.brand.toString()
                this.amountPerServing = foodInformation.amountPerServing.toBigDecimal()
                this.servingUnit = foodInformation.servingUnit
                this.status = PendingFoodStatus.PENDING
                this.createdBy = EntityID(foodToCreate.userPrincipal.id, U)
                this.createdAt = Instant.now()
            }

            // Insert nutrients
            UPFN.batchInsert(foodToCreate.foodInformation.nutrients) { d ->
                this[UPFN.foodId] = pfDao.id
                this[UPFN.nutrientId] = d.nutrientId
                this[UPFN.amount] = d.amount.toBigDecimal()
            }

            val nutrients = queryNutrientsForFood(UPFN, pfDao.id.value, foodToCreate.userPrincipal.id)
                .toClientFilter(isAppFood = true)
                .toCategoryGroups()

            pfDao.toDto(nutrients)
        }
    }

    override suspend fun updateStatus(pendingFoodReview: PendingFoodReview): PendingFood? = suspendTransaction {
        pendingFoodReview.let {
            val pfDao = PFDao.findById(it.pendingFoodId)
                ?: return@suspendTransaction null

            pfDao.apply {
                status = it.approvalStatus
                reviewedBy = EntityID(it.reviewerPrincipal.id, U)
                reviewedAt = Instant.now()
                it.rejectionReason?.let { reason -> rejectionReason = reason }
            }

            val nutrients = queryNutrientsForFood(UPFN, pfDao.id.value, it.reviewerPrincipal.id)
                .toClientFilter(isAppFood = true)
                .toCategoryGroups()

            pfDao.toDto(nutrients)
        }
    }

    override suspend fun countUserSubmissionsOfDay(userId: UUID, date: LocalDate): Int = suspendTransaction {
        val startOfDay = date.atStartOfDay().toInstant(ZoneOffset.UTC)
        val endOfDay = date.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC)

        PFDao.find {
            (PF.createdBy eq userId) and
            (PF.createdAt greaterEq startOfDay) and
            (PF.createdAt less endOfDay)
        }.count().toInt()
    }

    override suspend fun isDuplicate(food: FoodInformation<NutrientIdWithAmount>): Boolean = suspendTransaction {
        val pendingFoodBaseDaos = PFDao.find {
            (PF.name eq food.base.name) and
            (PF.brand eq food.base.brand.toString()) and
            (PF.amountPerServing eq food.base.amountPerServing.toBigDecimal()) and
            (PF.servingUnit eq food.base.servingUnit)
        }

        pendingFoodBaseDaos.any { foodBaseDao ->
            val foodBaseDaoNutrients = UPFN
                .select(UPFN.nutrientId, UPFN.amount)
                .where { UPFN.foodId eq foodBaseDao.id }
                .map { row ->
                    NutrientIdWithAmount(
                        nutrientId = row[UPFN.nutrientId].value,
                        amount = row[UPFN.amount].toDouble()
                    )
                }

            food.nutrients == foodBaseDaoNutrients
        }
    }

    override suspend fun delete(pendingFoodId: Int): Unit = suspendTransaction {
        PF.deleteWhere { id eq pendingFoodId }
    }
}