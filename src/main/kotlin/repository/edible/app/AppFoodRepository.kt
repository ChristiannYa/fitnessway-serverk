package com.example.repository.edible.app

import com.example.domain.*
import com.example.exception.AppEdibleReportAlreadyReviewedException
import com.example.mapping.*
import com.example.repository.edible.*
import com.example.utils.similarity
import com.example.utils.suspendTransaction
import com.example.utils.toEnum
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.*
import org.postgresql.util.PSQLException
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.*

class AppFoodRepository : IAppFoodRepository {

    override suspend fun findById(
        id: Int,
        userId: UUID
    ): Pair<AppEdibleRepoResult, String>? = suspendTransaction {

        val (aeDao, barcode) = AE
            .barcodesJoin()
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

        val (aeDao, barcode) = AE
            .barcodesJoin()
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

        val query = AE
            .barcodesJoin()
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
            .orderBy(AE.createdAt to SortOrder.DESC, AE.id to SortOrder.DESC)
            .limit(paginationCriteria.limit)
            .offset(paginationCriteria.offset)
            .map { AEDao.wrapRow(it) to it[AEB.barcode] }

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

    override suspend fun findReportById(id: Int): Pair<AERDao, List<AppEdibleReport.Reason>>? =
        suspendTransaction {
            val aerDao = AERDao.findById(id) ?: return@suspendTransaction null
            aerDao to queryReportReasons(aerDao.id.value)
        }

    override suspend fun getReports(
        paginationCriteria: PaginationCriteria<AppEdibleReportListPaginationCriteria>
    ): Result<PaginationQuery<AppEdibleReportQuery>> =
        suspendTransaction {

            val statusString = paginationCriteria.data.status.toString().lowercase()

            // Report counts per edible regardless of the status
            val reportCountByEdibleId = with(AER.id.count()) {
                AER
                    .select(AER.edibleId, this)
                    .groupBy(AER.edibleId)
                    .associate { it[AER.edibleId].value to it[this] }
            }

            // Query just the report IDs that match the status filter
            val matchingReports = AER
                .select(AER.id, AER.edibleId, AER.createdAt)
                .where { AER.status eq statusString }
                .map { Triple(it[AER.id].value, it[AER.edibleId].value, it[AER.createdAt]) }

            val totalCount = matchingReports.size.toLong()

            // Sort by count (desc), then created at (desc), then id (desc); then paginate
            val pagedReportIds = matchingReports
                .sortedWith(
                    compareByDescending<Triple<Int, Int, OffsetDateTime>> { (_, edibleId, _) ->
                        reportCountByEdibleId[edibleId] ?: 0
                    }
                        .thenByDescending { (_, _, createdAt) -> createdAt }
                        .thenByDescending { (count, _, _) -> count }
                )
                .drop(paginationCriteria.offset.toInt())
                .take(paginationCriteria.limit)
                .map { (count, _, _) -> count }

            if (pagedReportIds.isEmpty()) return@suspendTransaction Result.success(
                PaginationQuery(
                    emptyList(),
                    totalCount
                )
            )

            // Join full rows for the current page
            val rows = AE
                .barcodesJoin()
                .joinReports()
                .selectAll()
                .where { AER.id inList pagedReportIds }

            val rowsByReportId = rows.groupBy { it[AER.id].value }

            val appEdibleIds = pagedReportIds
                .mapNotNull { rowsByReportId[it]?.firstOrNull()?.get(AE.id)?.value }

            val edibleIdToNutrients =
                appEdibleIds.associateWith { queryNutrientsForFood(AEN, it, paginationCriteria.data.adminId) }

            val reports = pagedReportIds.map { reportId ->
                val rows = rowsByReportId[reportId]
                    ?: return@suspendTransaction Result.failure(
                        IllegalStateException("report #$reportId not found in joined result")
                    )

                val firstRow = rows.first()
                val aeDao = AEDao.wrapRow(firstRow)
                val aerDao = AERDao.wrapRow(firstRow)
                val barcode = firstRow[AEB.barcode]
                val reasons: List<AppEdibleReport.Reason> = rows.map { it[AERR.reason].toEnum() }

                val nutrients = edibleIdToNutrients[aeDao.id.value]
                    ?: return@suspendTransaction Result.failure(
                        IllegalStateException("app edible dao #${aeDao.id.value}'s nutrients not found")
                    )

                AppEdibleReportQuery(
                    edible = AppEdibleRepoResult(aeDao, nutrients) to barcode,
                    reports = listOf(aerDao to reasons)
                )
            }

            Result.success(PaginationQuery(reports, totalCount))
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

    // @TODO: update the `updatedAt` field as well
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

    override suspend fun report(report: AppEdibleReportWrite): AERDao = suspendTransaction {
        val aerDao = AERDao.new {
            this.edibleId = EntityID(report.edibleId, AE)
            this.reportedBy = EntityID(report.reportedBy, U)
            this.notes = report.notes
        }

        AERR.batchInsert(report.reasons) { reason ->
            this[AERR.reportId] = aerDao.id
            this[AERR.reason] = reason
        }

        aerDao
    }

    /**
     * Also checks if the report has already been reviewed to avoid having
     * the service layer make a DB call to find it and check for it
     * @throws AppEdibleReportAlreadyReviewedException
     */
    override suspend fun reviewReport(reportReview: AppEdibleReportReview): Pair<AERDao, List<AppEdibleReport.Reason>>? =
        suspendTransaction {
            val aerDao = AERDao
                .findById(reportReview.id)
                ?.also {
                    if (it.reviewedAt != null)
                        throw AppEdibleReportAlreadyReviewedException(reportReview.id)
                }
                ?.apply {
                    this.reviewedAt = Instant.now().atOffset(ZoneOffset.UTC)
                    this.reviewedBy = EntityID(reportReview.reviewedBy, U)
                    this.status = AppEdibleReport.Status.REVIEWED.toString().lowercase()
                }
                ?: return@suspendTransaction null

            aerDao to queryReportReasons(aerDao.id.value)
        }

    private suspend fun queryReportReasons(reportId: Int): List<AppEdibleReport.Reason> =
        suspendTransaction {
            AERR
                .selectAll()
                .where { AERR.reportId eq reportId }
                .map { it[AERR.reason].toEnum() }
        }
}
