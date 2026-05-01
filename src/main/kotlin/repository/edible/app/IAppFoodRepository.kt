package com.example.repository.edible.app

import com.example.domain.*
import com.example.mapping.AEDao
import java.util.*

interface IAppFoodRepository {
    suspend fun findById(id: Int, userId: UUID): Pair<AEDao, List<NutrientDataAmount>>?
    suspend fun create(foodToCreate: AppFoodCreate): Int
    suspend fun isDuplicate(base: EdibleBase, nutrientList: List<NutrientIdWithAmount>): Boolean
    suspend fun search(criteria: PaginationCriteria<AppFoodSearchPaginationCriteria>): PaginationQuery<FoodPreview>
}