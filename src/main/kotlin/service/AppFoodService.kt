package com.example.service

import com.example.domain.*
import com.example.mappers.toNutrientsByType
import com.example.mapping.toDto
import com.example.repository.edible.app.AppFoodRepository
import com.example.utils.extensions.sortBaseNutrients
import java.util.*

class AppFoodService(
    private val appFoodRepository: AppFoodRepository
) {
    suspend fun findById(id: Int, userId: UUID): AppFood? {
        val (aeDao, nutrientList) = appFoodRepository
            .findById(id, userId)
            ?: return null

        return aeDao
            .toDto(
                nutrients = nutrientList
                    .sortBaseNutrients()
                    .toNutrientsByType()
            )
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