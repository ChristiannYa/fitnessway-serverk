package com.example.repository.edible.app

import com.example.domain.*
import com.example.mapping.*
import com.example.repository.edible.AppEdibleRepoResult
import com.example.repository.edible.queryNutrientPreviews
import com.example.repository.edible.queryNutrientsForFood
import com.example.repository.edible.queryNutrientsForFoods
import com.example.utils.similarity
import com.example.utils.suspendTransaction
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.*
import org.postgresql.util.PSQLException
import java.time.Instant
import java.time.ZoneOffset
import java.util.*

class AppFoodRepository : IAppFoodRepository {

    override suspend fun findById(
        id: Int,
        userId: UUID
    ): Pair<AppEdibleRepoResult, String>? = suspendTransaction {

        val barcodesJoin = AE.join(
            joinType = JoinType.INNER,
            otherTable = AEB,
            onColumn = AE.id,
            otherColumn = AEB.edibleId
        )

        val (aeDao, barcode) = barcodesJoin
            .select(AE.columns + AEB.columns)
            .where { AE.id eq id }
            .map { AEDao.wrapRow(it) to it[AEB.barcode] }
            .firstOrNull()
            ?: return@suspendTransaction null

        val nutrients = queryNutrientsForFood(AEN, aeDao.id.value, userId)

        AppEdibleRepoResult(aeDao, nutrients) to barcode
    }

    override suspend fun findByBarcode(
        barcode: String,
        userId: UUID
    ): Pair<AppEdibleRepoResult, String>? = suspendTransaction {

        val barcodesJoin = AE.join(
            joinType = JoinType.INNER,
            otherTable = AEB,
            onColumn = AE.id,
            otherColumn = AEB.edibleId
        )

        val (aeDao, barcode) = barcodesJoin
            .select(AE.columns + AEB.columns)
            .where { AEB.barcode eq barcode }
            .map { AEDao.wrapRow(it) to it[AEB.barcode] }
            .firstOrNull()
            ?: return@suspendTransaction null

        val nutrients = queryNutrientsForFood(AEN, aeDao.id.value, userId)

        AppEdibleRepoResult(aeDao, nutrients) to barcode
    }

    override suspend fun findAdminSubmissions(
        paginationCriteria: PaginationCriteria<AppEdiblePaginationCriteria>
    ): Result<PaginationQuery<Pair<AppEdibleRepoResult, String>>> = suspendTransaction {

        val criteria = paginationCriteria.data

        val barcodesJoin = AE.join(
            joinType = JoinType.INNER,
            otherTable = AEB,
            onColumn = AE.id,
            otherColumn = AEB.edibleId
        )

        val query = barcodesJoin
            .select(AE.columns + AEB.columns)
            .where { AE.createdBy eq criteria.createdBy }
            .apply {
                criteria.createdAt?.let {
                    andWhere {
                        (AE.createdAt greaterEq it.startOffset) and
                        (AE.createdAt less it.endOffset)
                    }
                }
            }

        val queryTotalCount = query.count()

        val aeDaoToBarcodeList = query
            .limit(paginationCriteria.limit)
            .offset(paginationCriteria.offset)
            .map { row ->
                AEDao.wrapRow(row) to row[AEB.barcode]
            }

        val appEdibleIds = aeDaoToBarcodeList.map { (aeDao, _) -> aeDao.id.value }

        val appEdiblesNutrientMapById = queryNutrientsForFoods(
            AEN,
            appEdibleIds,
            paginationCriteria.data.createdBy
        )

        val aeDaoToBarcodeMapById = aeDaoToBarcodeList.associateBy { (aeDao, _) -> aeDao.id.value }

        val appEdiblesData: List<Pair<AppEdibleRepoResult, String>> = appEdibleIds.map { id ->
            val (aeDao, barcode) = aeDaoToBarcodeMapById[id]
                ?: return@suspendTransaction Result.failure(
                    IllegalStateException("app edible dao #$id not found")
                )

            appEdiblesNutrientMapById[id]
                ?.let { nutrients ->
                    AppEdibleRepoResult(
                        edibleDao = aeDao,
                        nutrients = nutrients
                    ) to barcode
                }
                ?: return@suspendTransaction Result.failure(
                    IllegalStateException("app edible dao #$id's nutrients not found")
                )
        }

        Result.success(PaginationQuery(appEdiblesData, queryTotalCount))
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
                this.createdAt = Instant.now().atOffset(ZoneOffset.UTC)
            }
        }

        AEN.batchInsert(foodToCreate.nutrientList) { nutrient ->
            this[AEN.edibleId] = aeDao.id.value
            this[AEN.nutrientId] = nutrient.id
            this[AEN.amount] = nutrient.amount.toBigDecimal()
        }

        aeDao to queryNutrientsForFood(AEN, aeDao.id.value, foodToCreate.createdBy)
    }

    override suspend fun updateBase(
        edibleId: Int,
        base: EdibleBase
    ) = suspendTransaction {

        AE.update(where = { (AE.id eq edibleId) }) {
            it[AE.name] = base.name
            it[AE.brand] = base.brand.toString()
            it[AE.amountPerServing] = base.amountPerServing.toBigDecimal()
            it[AE.servingUnit] = base.servingUnit
        }

        Unit
    }

    override suspend fun updateNutrients(
        edibleId: Int,
        nutrients: List<NutrientIdWithAmount>
    ) = suspendTransaction {

        AEN.batchUpsert(nutrients) { nutrient ->
            this[AEN.edibleId] = edibleId
            this[AEN.nutrientId] = nutrient.id
            this[AEN.amount] = nutrient.amount.toBigDecimal()
        }

        Unit
    }

    override suspend fun updateType(
        edibleId: Int,
        type: EdibleType
    ) = suspendTransaction {

        AE.update(where = { (AE.id eq edibleId) }) {
            it[AE.edibleType] = type
        }

        Unit
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
                        id = row[AEN.nutrientId].value,
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