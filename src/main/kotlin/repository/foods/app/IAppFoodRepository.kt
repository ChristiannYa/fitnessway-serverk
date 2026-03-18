package com.example.repository.foods.app

import com.example.domain.*
import java.util.*

interface IAppFoodRepository {
    suspend fun findById(id: Int, userId: UUID): AppFood?
    suspend fun create(foodToCreate: AppFoodCreate): Int
    suspend fun isDuplicate(food: FoodInformation<NutrientIdWithAmount>): Boolean
    suspend fun search(criteria: PaginationCriteria<AppFoodSearchPaginationCriteria>): PaginationQuery<FoodSearchResult>
}