package com.example.service

import com.example.domain.AppFoodSearchPaginationCriteria
import com.example.domain.FoodPreview
import com.example.domain.PaginationCriteria
import com.example.domain.PaginationResult
import com.example.repository.foods.app.AppFoodRepository
import java.util.*

class AppFoodService(
    private val appFoodRepository: AppFoodRepository
) {
    suspend fun findById(id: Int, userId: UUID) = appFoodRepository.findById(id, userId)

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