package com.example.repository.edible.app

import com.example.domain.*
import com.example.mapping.AEDao
import com.example.repository.edible.AppEdibleRepoResult
import java.util.*

interface IAppFoodRepository {
    suspend fun findById(id: Int, userId: UUID): Pair<AEDao, List<NutrientDataAmount>>?
    suspend fun findByBarcode(barcode: String, userId: UUID): Pair<AEDao, List<NutrientDataAmount>>?
    suspend fun findPagination(paginationCriteria: PaginationCriteria<AppEdiblePaginationCriteria>): Result<PaginationQuery<Pair<AppEdibleRepoResult, String>>>
    suspend fun submit(foodToCreate: AppFoodCreate): Pair<AEDao, List<NutrientDataAmount>>
    suspend fun setBarcode(barcode: String, edibleId: Int): DatabaseResult
    suspend fun isDuplicate(base: EdibleBase, nutrientList: List<NutrientIdWithAmount>): Boolean
    suspend fun search(criteria: PaginationCriteria<AppFoodSearchPaginationCriteria>): PaginationQuery<FoodPreview>
}