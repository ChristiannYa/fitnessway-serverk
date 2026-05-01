package com.example.repository.edible.log

import com.example.domain.*
import java.util.*

interface IFoodLogRepository {
    suspend fun findById(id: Int, userId: UUID): EdibleLogBuildData?
    suspend fun findByDate(userId: UUID, isUserPremium: Boolean, range: InstantRange): Result<List<EdibleLogBuildData>>
    suspend fun findLatest(criteria: PaginationCriteria<RecentlyLoggedFoodsPaginationCriteria>): PaginationQuery<FoodPreview>
    suspend fun getBase(userId: UUID, foodLogId: Int): FoodLogBase?
    suspend fun add(data: FoodLogAdd): Int
    suspend fun updateServings(userId: UUID, foodLogId: Int, servings: Double): Boolean
}