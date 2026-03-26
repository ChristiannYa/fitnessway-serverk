package com.example.service

import com.example.domain.AppFoodSearchPaginationCriteria
import com.example.domain.FoodSearchResult
import com.example.domain.PaginationCriteria
import com.example.domain.PaginationResult
import com.example.repository.foods.app.AppFoodRepository
import java.util.*

class AppFoodService(
    private val appFoodRepository: AppFoodRepository
) {
    suspend fun findById(id: Int, userId: UUID) = appFoodRepository.findById(id, userId)

    suspend fun search(
        paginationCriteria: PaginationCriteria<AppFoodSearchPaginationCriteria>
    ): PaginationResult<FoodSearchResult> {
        val paginationQuery = appFoodRepository.search(paginationCriteria)

        return PaginationResult(
            data = paginationQuery.data,
            totalCount = paginationQuery.totalCount,
            pageCount = paginationCriteria.calcPageCount(paginationQuery.totalCount.toDouble()),
            currentPage = paginationCriteria.calcCurrentPage()
        )
    }
}