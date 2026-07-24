package com.example.service

import com.example.config.RewardConfig
import com.example.config.RewardConfig.applyMultiplier
import com.example.domain.*
import com.example.dto.AppEdibleReportRequest
import com.example.dto.AppEdibleWriteRequest
import com.example.exception.*
import com.example.mappers.toList
import com.example.mappers.toNutrientsByType
import com.example.mapping.AERDao
import com.example.mapping.AERUDao
import com.example.mapping.toDto
import com.example.repository.edible.*
import com.example.repository.edible.app.IAppFoodRepository
import com.example.repository.user.IUserRepository
import com.example.repository.user.wallets.IUserWalletRepository
import com.example.utils.date_time.TimeConverter
import com.example.utils.suspendTransaction
import com.example.utils.toEnum
import io.ktor.server.plugins.*
import java.util.*
import kotlin.time.toKotlinInstant

private data class ReportData(
    val reward: Double,
    val transactionType: UserTransactionType
)

class AppFoodService(
    private val appFoodRepository: IAppFoodRepository,
    private val userRepository: IUserRepository,
    private val userWalletRepository: IUserWalletRepository,
    private val timeConverter: TimeConverter
) {

    companion object {
        private fun UserType.getReportData() =
            ReportData(
                reward = when (this) {
                    UserType.USER -> 1.0
                    UserType.CONTRIBUTOR -> 1.5
                    UserType.ADMIN -> 1.0
                },
                transactionType = UserTransactionType.APP_EDIBLE_REPORT_CONTRIBUTION
            )

    }

    private fun isBarcodeValid(barcode: String): Boolean {
        // Must be 12 (UPC-A) or 13 (EAN-13) digits
        if (!barcode.matches(Regex("^\\d{12,13}$"))) return false

        val digitArray = barcode
            .map { it.digitToInt() }
            .toMutableList()

        val checkDigit = digitArray.removeLast()

        val isUpc = digitArray.size == 11
        var sum = 0

        digitArray.forEachIndexed { i, digit ->
            // UPC-A: even positions ×3, odd ×1
            // EAN-13: odd positions ×3, even ×1
            val isTriplePosition = if (isUpc) i % 2 == 0 else i % 2 != 0
            val multiplier = if (isTriplePosition) 3 else 1

            sum += digit * multiplier
        }

        return (10 - sum % 10) % 10 == checkDigit
    }

    private suspend fun find(
        finder: suspend () -> Pair<AppEdibleRepoResult, String>?
    ): AppEdibleData? = finder()?.let { (repoResult, barcode) ->
        AppEdibleData(
            edible = repoResult.edibleDao.toDto(
                nutrients = repoResult.nutrients
                    .sortedBy { it.bySortOrder }
                    .toNutrientsByType()
            ),
            barcode
        )
    }

    suspend fun findById(id: Int, userId: UUID): AppEdibleData? =
        find { appFoodRepository.findById(id, userId) }

    suspend fun findByBarCode(barcode: String, userId: UUID): AppEdibleData? =
        find { appFoodRepository.findByBarcode(barcode, userId) }

    suspend fun findAdminSubmissions(
        userPrincipal: UserPrincipal,
        createdAt: String?,
        limit: Int,
        offset: Long
    ): PaginationResult<AppEdibleData> {

        val createdAtRange = createdAt?.let {
            timeConverter
                .toUtcRangeResult(it, userPrincipal.timezone)
                .getOrElse { ex ->
                    throw BadRequestException(
                        "user time convertion failed: ${ex.message}"
                    )
                }
        }

        val paginationCriteria = PaginationCriteria(
            data = AppEdiblePaginationCriteria(
                createdBy = userPrincipal.id,
                createdAt = createdAtRange,
            ),
            limit = limit,
            offset = offset
        )

        val paginationQuery = appFoodRepository
            .findAdminSubmissions(paginationCriteria)
            .getOrThrow()

        return PaginationResult(
            data = paginationQuery.data.map { (repoRes, barcode) ->
                AppEdibleData(
                    edible = repoRes.edibleDao.toDto(
                        repoRes.nutrients
                            .sortedBy { it.bySortOrder }
                            .toNutrientsByType()
                    ),
                    barcode = barcode
                )
            },
            totalCount = paginationQuery.totalCount,
            pageCount = paginationCriteria.calcPageCount(paginationQuery.totalCount.toDouble()),
            currentPage = paginationCriteria.calcCurrentPage()
        )
    }

    suspend fun getReports(
        adminId: UUID,
        status: AppEdibleReport.Status,
        limit: Int,
        offset: Long
    ): PaginationResult<AppEdibleData> {
        val paginationCriteria = PaginationCriteria(
            data = AppEdibleReportListPaginationCriteria(
                status = status,
                adminId = adminId
            ),
            limit = limit,
            offset = offset
        )

        val paginationQuery = appFoodRepository
            .getReports(paginationCriteria)
            .getOrThrow()

        return PaginationResult(
            data = paginationQuery.data.map {
                val (edibleRepoRes, barcode) = it.edible
                AppEdibleData(
                    edible = edibleRepoRes.edibleDao.toDto(
                        edibleRepoRes.nutrients
                            .sortedBy { n -> n.bySortOrder }
                            .toNutrientsByType()
                    ),
                    barcode = barcode,
                    reports = it.reports.map { (aerDao, reasons) -> aerDao.toDto(reasons) }
                )
            },
            totalCount = paginationQuery.totalCount,
            pageCount = paginationCriteria.calcPageCount(paginationQuery.totalCount.toDouble()),
            currentPage = paginationCriteria.calcCurrentPage()
        )
    }

    suspend fun submit(
        req: AppEdibleWriteRequest,
        userId: UUID
    ): AppFood = suspendTransaction {

        if (!isBarcodeValid(req.barcode)) throw InvalidEdibleBarcodeException()

        val isAlreadyInApp = appFoodRepository.isDuplicate(req.edibleRequest.base, req.edibleRequest.nutrients)
        if (isAlreadyInApp) throw EdibleAlreadyExistsException(req.edibleRequest.edibleType.toEnum())

        val (aeDao, nutrientList) = appFoodRepository.submit(
            foodToCreate = AppFoodCreate(
                createdBy = userId,
                base = req.edibleRequest.base,
                nutrientList = req.edibleRequest.nutrients,
                edibleType = req.edibleRequest.edibleType.toEnum()
            )
        )

        val appEdible = aeDao.toDto(
            nutrientList
                .sortedBy { it.bySortOrder }
                .toNutrientsByType()
        )

        setBarcode(
            barcode = req.barcode,
            edibleId = appEdible.id,
            edibleType = req.edibleRequest.edibleType.toEnum()
        )

        appEdible
    }

    suspend fun update(
        userId: UUID,
        edibleId: Int,
        updateInfo: AppEdibleWriteRequest
    ) = suspendTransaction {

        val (repoResult, barcodeDb) = appFoodRepository
            .findById(edibleId, userId)
            ?: throw EdibleNotFoundException("app edible #$edibleId not found when updating")

        val originalAppEdible = repoResult.edibleDao.toDto(repoResult.nutrients.toNutrientsByType())

        if (originalAppEdible.information.base != updateInfo.edibleRequest.base) {
            appFoodRepository.updateBase(edibleId, updateInfo.edibleRequest.base)
        }

        if (repoResult.edibleDao.edibleType != updateInfo.edibleRequest.edibleType.toEnum<EdibleType>()) {
            appFoodRepository.updateType(edibleId, updateInfo.edibleRequest.edibleType.toEnum())
        }

        appFoodRepository.updateNutrients(edibleId, updateInfo.edibleRequest.nutrients)

        if (barcodeDb != updateInfo.barcode) {
            appFoodRepository.setBarcode(barcode = updateInfo.barcode, repoResult.edibleDao.id.value)
        }
    }

    suspend fun setBarcode(
        barcode: String,
        edibleId: Int,
        edibleType: EdibleType
    ) {
        if (!isBarcodeValid(barcode)) throw InvalidEdibleBarcodeException()

        appFoodRepository
            .setBarcode(barcode, edibleId)
            .throwIfNotSuccess("${edibleType.toString().lowercase()} barcode")
    }

    suspend fun search(
        criteria: PaginationCriteria<AppFoodSearchPaginationCriteria>
    ): PaginationResult<FoodPreview> {
        val pagination = appFoodRepository.search(criteria)

        return PaginationResult(
            data = pagination.data,
            totalCount = pagination.totalCount,
            pageCount = criteria.calcPageCount(pagination.totalCount.toDouble()),
            currentPage = criteria.calcCurrentPage()
        )
    }

    suspend fun report(
        req: AppEdibleReportRequest,
        userId: UUID
    ): AppEdibleReport = suspendTransaction {

        val aerDao = appFoodRepository
            .report(
                AppEdibleReportWrite(
                    edibleId = req.edibleId,
                    reportedBy = userId,
                    reasons = req.reasons,
                    notes = req.notes
                )
            )

        req.updatedEdible?.let { updatedEdible ->
            appFoodRepository.submitEdibleInReport(
                reportId = aerDao.id.value,
                writeData = updatedEdible.let { (_, e) -> e.toRepoWrite() }
            )
        }

        aerDao.toDto(req.reasons.map { it.toEnum() })
    }

    suspend fun reviewReport(
        reportId: Int,
        reviewerId: UUID,
    ): Unit = suspendTransaction {

        // 1: Make sure report is found
        val (aerDao, _) = appFoodRepository
            .findReportById(reportId)
            ?: throw ItemNotFoundException("report")

        // 1.1: Make sure report is not reviewed already
        if (aerDao.reviewedAt != null) throw AppEdibleReportAlreadyReviewedException(reportId)

        // 2: Review report
        val aerDaoReview = appFoodRepository.reviewReport(AppEdibleReportReview(reportId, reviewerId))

        // 3: BUILD UPDATED/FIXED EDIBLE (IF UPDATE IS PRESENT)
        appFoodRepository
            .findReportUpdateById(reportId, reviewerId)
            ?.let { edibleRepoResult ->
                val original = find { appFoodRepository.findById(edibleRepoResult.edibleDao.id.value, reviewerId) }
                    ?: throw ItemNotFoundException("report update edible")

                val updated = original.updateFromReport(edibleRepoResult, aerDaoReview, reportId)
                appFoodRepository.update(updated.edible.id, reviewerId, updated.toDbWrite())
            }

        // 4: REWARD USER
        aerDao.reportedBy?.let {
            userRepository
                .findById(it.value)
                ?.let { reporterFound ->
                    userWalletRepository.addCurrency(
                        UserAddCurrency(
                            userId = reporterFound.id,
                            amount = RewardConfig.APP_EDIBLE_REPORT_REWARD.applyMultiplier(reporterFound.type),
                            transactionType = UserTransactionType.APP_EDIBLE_REPORT_CONTRIBUTION
                        )
                    )
                }
        }
    }
}

// -------------------------------------------
// MAPPER FUNCTIONS JUST TO SHORTEN MAIN CODE
// -------------------------------------------

private fun AppEdibleData.updateFromReport(
    updatedEdibleFromRepo: EdibleRepoResult<AERUDao, NutrientDataAmount>,
    aerDaoReview: AERDao,
    reportId: Int,
): AppEdibleData {
    val (aeruDao, nutrients) = updatedEdibleFromRepo
    return this.copy(
        edible = this.edible.copy(
            information = this.edible.information.copy(
                base = EdibleBase(
                    name = aeruDao.name,
                    brand = aeruDao.brand,
                    amountPerServing = aeruDao.amountPerServing.toDouble(),
                    servingUnit = aeruDao.servingUnit
                ),
                nutrients = nutrients.toNutrientsByType(),
                type = aeruDao.edibleType
            )
        ),
        barcode = aeruDao.barcode,
        reports = this.reports.map {
            if (it.id != reportId) it
            else it.copy(
                status = aerDaoReview.status.toEnum(),
                reviewedAt = aerDaoReview.reviewedAt?.toInstant()?.toKotlinInstant(),
                reviewedBy = aerDaoReview.reviewedBy?.value
            )
        }
    )
}

private fun AppEdibleData.toDbWrite() = AppEdibleRepoWrite(
    base = this.edible.information.base,
    nutrientList = this.edible.information.nutrients
        .toList()
        .map { NutrientIdWithAmount(id = it.data.base.id, amount = it.amount) },
    type = this.edible.information.type,
    barcode = this.barcode
)

private fun AppEdibleWriteRequest.toRepoWrite() = AppEdibleRepoWrite(
    base = this.edibleRequest.base,
    nutrientList = this.edibleRequest.nutrients,
    type = this.edibleRequest.edibleType.toEnum(),
    barcode = this.barcode,
)
