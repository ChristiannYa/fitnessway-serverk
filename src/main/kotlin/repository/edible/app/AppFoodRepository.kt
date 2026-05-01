package com.example.repository.edible.app

import com.example.domain.*
import com.example.mapping.AE
import com.example.mapping.AEDao
import com.example.mapping.AEN
import com.example.mapping.U
import com.example.repository.edible.queryNutrientPreviews
import com.example.repository.edible.queryNutrientsForFood
import com.example.utils.similarity
import com.example.utils.suspendTransaction
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.lowerCase
import java.util.*

class AppFoodRepository : IAppFoodRepository {

    override suspend fun findById(
        id: Int,
        userId: UUID
    ): Pair<AEDao, List<NutrientDataAmount>>? = suspendTransaction {

        val aeDao = AEDao
            .findById(id)
            ?: return@suspendTransaction null

        aeDao to queryNutrientsForFood(AEN, aeDao.id.value, userId)
    }

    override suspend fun create(foodToCreate: AppFoodCreate): Int = suspendTransaction {
        val aeDao = foodToCreate.base.let { foodBase ->
            AEDao.new {
                this.name = foodBase.name
                this.brand = foodBase.brand.toString()
                this.amountPerServing = foodBase.amountPerServing.toBigDecimal()
                this.servingUnit = foodBase.servingUnit
                this.edibleType = foodToCreate.edibleType
                this.createdBy = EntityID(foodToCreate.createdBy, U)
            }
        }

        AEN.batchInsert(foodToCreate.nutrientList) { nutrient ->
            this[AEN.edibleId] = aeDao.id.value
            this[AEN.nutrientId] = nutrient.nutrientId
            this[AEN.amount] = nutrient.amount.toBigDecimal()
        }

        aeDao.id.value
    }

    override suspend fun isDuplicate(
        base: EdibleBase,
        nutrientList: List<NutrientIdWithAmount>
    ): Boolean = suspendTransaction {

        val aeDaos = AEDao.find {
            (AE.name eq base.name) and
            (AE.brand eq base.brand.toString()) and
            (AE.amountPerServing eq base.amountPerServing.toBigDecimal()) and
            (AE.servingUnit eq base.servingUnit)
        }

        aeDaos.any { appFoodDao ->
            val appFoodDaoNutrients = AEN
                .select(AEN.nutrientId, AEN.amount)
                .where { AEN.edibleId eq appFoodDao.id }
                .map { row ->
                    NutrientIdWithAmount(
                        nutrientId = row[AEN.nutrientId].value,
                        amount = row[AEN.amount].toDouble()
                    )
                }

            nutrientList == appFoodDaoNutrients
        }
    }

    // @TODO: Should filter by edible type
    override suspend fun search(
        criteria: PaginationCriteria<AppFoodSearchPaginationCriteria>
    ): PaginationQuery<FoodPreview> = suspendTransaction {
        val query = criteria.data.query
        val matched = AEDao.find {
            AE.name.lowerCase() like "%${query.lowercase()}%"
        }

        val afDaos = matched
            .orderBy(
                similarity(AE.name, query) to SortOrder.DESC,
                AE.id to SortOrder.ASC
            )
            .limit(criteria.limit)
            .offset(criteria.offset)
            .toList()

        val foodIds = afDaos.map { it.id.value }
        val nutrientPreviews = queryNutrientPreviews(AEN, foodIds, criteria.data.userId)

        val data = afDaos.map { afDao ->
            FoodPreview(
                id = afDao.id.value,
                base = EdibleBase(
                    name = afDao.name,
                    brand = afDao.brand.ifEmpty { "~" },
                    amountPerServing = afDao.amountPerServing.toDouble(),
                    servingUnit = afDao.servingUnit
                ),
                nutrientPreview = nutrientPreviews[afDao.id.value] ?: NutrientPreview(),
                source = LogSource.APP
            )
        }

        PaginationQuery(data, matched.count())
    }
}