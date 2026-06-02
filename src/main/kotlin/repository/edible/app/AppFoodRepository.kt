package com.example.repository.edible.app

import com.example.domain.*
import com.example.mapping.*
import com.example.repository.edible.queryNutrientPreviews
import com.example.repository.edible.queryNutrientsForFood
import com.example.utils.similarity
import com.example.utils.suspendTransaction
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.*
import org.postgresql.util.PSQLException
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

    override suspend fun findByBarcode(
        barcode: String,
        userId: UUID
    ): Pair<AEDao, List<NutrientDataAmount>>? = suspendTransaction {

        val aeDao: AEDao = AE
            .join(
                joinType = JoinType.INNER,
                otherTable = AEB,
                onColumn = AE.id,
                otherColumn = AEB.edibleId
            )
            .select(AE.columns)
            .where { (AEB.barcode eq barcode) }
            .firstOrNull()
            ?.let { AEDao.wrapRow(it) }
            ?: return@suspendTransaction null

        aeDao to queryNutrientsForFood(AEN, aeDao.id.value, userId)
    }

    override suspend fun submit(
        foodToCreate: AppFoodCreate
    ): Pair<AEDao, List<NutrientDataAmount>> = suspendTransaction {

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

        aeDao to queryNutrientsForFood(AEN, aeDao.id.value, foodToCreate.createdBy)
    }

    override suspend fun setBarcode(
        barcode: String,
        edibleId: Int
    ): DatabaseResult = suspendTransaction {
        try {
            val insertCount = AEB
                .insert {
                    it[AEB.barcode] = barcode
                    it[AEB.edibleId] = edibleId
                }
                .insertedCount

            if (insertCount != 1) DatabaseResult.UnexpectedInsertCount
            else DatabaseResult.Success

        } catch (ex: ExposedSQLException) {
            val cause = ex.cause

            if (cause is PSQLException && cause.sqlState == "23505")
                DatabaseResult.Duplicate
            else DatabaseResult.UnexpectedError(ex.message.toString())
        }
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

    override suspend fun search(
        criteria: PaginationCriteria<AppFoodSearchPaginationCriteria>
    ): PaginationQuery<FoodPreview> = suspendTransaction {
        val query = criteria.data.query
        val matched = AEDao.find {
            (AE.name.lowerCase() like "%${query.lowercase()}%") and
            (AE.edibleType eq criteria.data.edibleType)
        }

        val count = matched.count()

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

        PaginationQuery(data, count)
    }
}