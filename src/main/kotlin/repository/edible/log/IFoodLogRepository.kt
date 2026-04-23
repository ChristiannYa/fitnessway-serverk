package com.example.repository.edible.log

import com.example.domain.*
import java.util.*

interface IFoodLogRepository {
    suspend fun findById(userId: UUID, id: Int, isUserPremium: Boolean): FoodLog?
    suspend fun findByDate(userId: UUID, isUserPremium: Boolean, range: InstantRange): Result<List<FoodLog>>
    suspend fun findLatest(criteria: PaginationCriteria<RecentlyLoggedFoodsPaginationCriteria>): PaginationQuery<FoodPreview>
    suspend fun getBaseData(userId: UUID, foodLogId: Int): FoodLogBase?
    suspend fun add(data: FoodLogAdd): Int
    suspend fun updateServings(userId: UUID, foodLogId: Int, servings: Double): Boolean
}