package com.example.service

import com.example.config.RewardConfig
import com.example.config.RewardConfig.applyMultiplier
import com.example.domain.*
import com.example.dto.AppEdibleReportRequest
import com.example.dto.AppEdibleWriteRequest
import com.example.exception.AppEdibleReportNotFoundException
import com.example.exception.EdibleAlreadyExistsException
import com.example.exception.EdibleNotFoundException
import com.example.exception.InvalidEdibleBarcodeException
import com.example.mappers.toNutrientsByType
import com.example.mapping.toDto
import com.example.repository.edible.AppEdibleRepoResult
import com.example.repository.edible.AppEdibleReportReview
import com.example.repository.edible.AppEdibleReportWrite
import com.example.repository.edible.app.IAppFoodRepository
import com.example.repository.user.IUserRepository
import com.example.repository.user.wallets.IUserWalletRepository
import com.example.utils.date_time.TimeConverter
import com.example.utils.suspendTransaction
import com.example.utils.toEnum
import io.ktor.server.plugins.*
import java.util.*

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

        val (repoResult, _) = appFoodRepository
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

    suspend fun report(req: AppEdibleReportRequest, userId: UUID): AppEdibleReport =
        appFoodRepository
            .report(
                AppEdibleReportWrite(
                    edibleId = req.edibleId,
                    reportedBy = userId,
                    reasons = req.reasons,
                    notes = req.notes
                )
            )
            .toDto(req.reasons.map { it.toEnum() })

    suspend fun reviewReport(reportId: Int, reviewerId: UUID): AppEdibleReport = suspendTransaction {

        val report = appFoodRepository
            .reviewReport(AppEdibleReportReview(reportId, reviewerId))
            ?.let { (aerDao, reasons) -> aerDao.toDto(reasons) }
            ?: throw AppEdibleReportNotFoundException(reportId)

        report.reportedBy?.let {
            userRepository
                .findById(it)
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

        report
    }
}
