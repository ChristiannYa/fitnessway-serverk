package com.example.repository.edible.pending

import com.example.domain.*
import com.example.mapping.PE
import com.example.mapping.PEDao
import com.example.mapping.U
import com.example.mapping.UPEN
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
    override suspend fun findById(
        id: Int,
        createdById: UUID,
        reviewerId: UUID?
    ): Pair<PEDao, List<NutrientDataAmount>>? = suspendTransaction {
        val peDao = PEDao.find {
            (PE.id eq id) and
            (PE.createdBy eq createdById)
        }.firstOrNull() ?: return@suspendTransaction null

        val nutrients = queryNutrientsForFood(UPEN, peDao.id.value, reviewerId ?: createdById)

        peDao to nutrients
    }

    override suspend fun findPaginated(
        paginationCriteria: PaginationCriteria<PendingFoodsPaginationCriteria>
    ): Result<PaginationQuery<Pair<PEDao, List<NutrientDataAmount>>>> = suspendTransaction {
        val criteria = paginationCriteria.data

        val query = run {
            val pfJoin: Join = PE.join(
                joinType = JoinType.INNER,
                otherTable = U,
                onColumn = PE.createdBy,
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
                        .where { PE.createdBy eq userScope.id }
            }
        }.apply {
            criteria.status?.let { andWhere { PE.status eq it } }
            criteria.edibleType?.let { andWhere { PE.edibleType eq it } }
        }

        val queryCount = query.count()

        val peDao = query
            .limit(paginationCriteria.limit)
            .offset(paginationCriteria.offset)
            .map { row -> PEDao.wrapRow(row) }

        val foodIds = peDao.map { it.id.value }

        val foodNutrients = queryNutrientsForFoods(
            UPEN,
            foodIds,
            paginationCriteria.data.userId
        )

        val pfDaoMap = peDao.associateBy { it.id.value }
        val foods = foodIds.map { foodId ->
            val pfDao = pfDaoMap[foodId] ?: return@suspendTransaction Result.failure(
                IllegalStateException("pending food dao #${foodId} not found")
            )

            foodNutrients[foodId]?.let { nutrients ->
                pfDao to nutrients
            } ?: return@suspendTransaction Result.failure(
                IllegalStateException("pending food dao #${foodId}'s nutrients not found")
            )
        }

        Result.success(PaginationQuery(foods, queryCount))
    }

    override suspend fun create(
        foodToCreate: PendingFoodCreate
    ): Pair<PEDao, List<NutrientDataAmount>> = suspendTransaction {

        val peDao = PEDao.new {
            this.name = foodToCreate.base.name
            this.brand = foodToCreate.base.brand.toString()
            this.amountPerServing = foodToCreate.base.amountPerServing.toBigDecimal()
            this.servingUnit = foodToCreate.base.servingUnit
            this.edibleType = foodToCreate.edibleType
            this.status = PendingFoodStatus.PENDING
            this.createdBy = EntityID(foodToCreate.userId, U)
            this.createdAt = Instant.now()
        }

        UPEN.batchInsert(foodToCreate.nutrientList) { d ->
            this[UPEN.edibleId] = peDao.id
            this[UPEN.nutrientId] = d.nutrientId
            this[UPEN.amount] = d.amount.toBigDecimal()
        }

        peDao to queryNutrientsForFood(UPEN, peDao.id.value, foodToCreate.userId)
    }

    override suspend fun updateStatus(
        pendingFoodReview: PendingFoodReview
    ): Pair<PEDao, List<NutrientDataAmount>>? = suspendTransaction {
        val peDao = PEDao.find {
            (PE.id eq pendingFoodReview.pendingFoodId) and
            (PE.createdBy eq pendingFoodReview.createdById)
        }.firstOrNull() ?: return@suspendTransaction null

        peDao.apply {
            status = pendingFoodReview.approvalStatus
            reviewedBy = EntityID(pendingFoodReview.reviewerPrincipal.id, U)
            reviewedAt = Instant.now()
            pendingFoodReview.rejectionReason?.let { reason -> rejectionReason = reason }
        }

        peDao to queryNutrientsForFood(UPEN, peDao.id.value, pendingFoodReview.reviewerPrincipal.id)
    }

    override suspend fun countUserSubmissionsOfDay(userId: UUID, date: LocalDate): Int = suspendTransaction {
        val startOfDay = date.atStartOfDay().toInstant(ZoneOffset.UTC)
        val endOfDay = date.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC)

        PEDao.find {
            (PE.createdBy eq userId) and
            (PE.createdAt greaterEq startOfDay) and
            (PE.createdAt less endOfDay)
        }.count().toInt()
    }

    override suspend fun isDuplicate(
        base: EdibleBase,
        nutrientList: List<NutrientIdWithAmount>
    ): Boolean = suspendTransaction {
        val pendingFoodBaseDaos = PEDao.find {
            (PE.name eq base.name) and
            (PE.brand eq base.brand.toString()) and
            (PE.amountPerServing eq base.amountPerServing.toBigDecimal()) and
            (PE.servingUnit eq base.servingUnit)
        }

        pendingFoodBaseDaos.any { foodBaseDao ->
            val foodBaseDaoNutrients = UPEN
                .select(UPEN.nutrientId, UPEN.amount)
                .where { UPEN.edibleId eq foodBaseDao.id }
                .map { row ->
                    NutrientIdWithAmount(
                        nutrientId = row[UPEN.nutrientId].value,
                        amount = row[UPEN.amount].toDouble()
                    )
                }

            nutrientList == foodBaseDaoNutrients
        }
    }

    override suspend fun delete(pendingFoodId: Int, userId: UUID) = suspendTransaction {
        PE.deleteWhere {
            (this.id eq pendingFoodId) and
            (this.createdBy eq userId)
        }
    }
}