package com.example.service

import com.example.domain.*
import com.example.dto.AppEdibleSubmitRequest
import com.example.exception.EdibleAlreadyExistsException
import com.example.exception.InvalidEdibleBarcodeException
import com.example.mappers.toNutrientsByType
import com.example.mapping.AEDao
import com.example.mapping.toDto
import com.example.repository.edible.app.AppFoodRepository
import com.example.utils.extensions.sortBaseNutrients
import com.example.utils.suspendTransaction
import com.example.utils.toEnum
import java.util.*

class AppFoodService(
    private val appFoodRepository: AppFoodRepository
) {
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

    private suspend fun find(finder: suspend () -> Pair<AEDao, List<NutrientDataAmount>>?): AppFood? {
        val (aeDao, nutrientList) = finder() ?: return null

        return aeDao
            .toDto(
                nutrients = nutrientList
                    .sortBaseNutrients()
                    .toNutrientsByType()
            )
    }

    suspend fun findById(id: Int, userId: UUID): AppFood? =
        find { appFoodRepository.findById(id, userId) }

    suspend fun findByBarCode(barcode: String, userId: UUID): AppFood? =
        find { appFoodRepository.findByBarcode(barcode, userId) }

    suspend fun submit(
        req: AppEdibleSubmitRequest,
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
                .sortBaseNutrients()
                .toNutrientsByType()
        )

        setBarcode(
            barcode = req.barcode,
            edibleId = appEdible.id,
            edibleType = req.edibleRequest.edibleType.toEnum()
        )

        appEdible
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
}