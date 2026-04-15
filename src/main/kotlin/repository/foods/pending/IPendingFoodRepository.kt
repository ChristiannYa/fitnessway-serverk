package com.example.repository.foods.pending

import com.example.domain.*
import com.example.mapping.PFDao
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.*

interface IPendingFoodRepository {
    suspend fun findPaginated(
        paginationCriteria: PaginationCriteria<PendingFoodsPaginationCriteria>
    ): Result<PaginationQuery<Pair<PFDao, List<NutrientDataAmount>>>>

    suspend fun findById(
        id: Int,
        createdById: UUID,
        reviewerId: UUID? = null
    ): Pair<PFDao, List<NutrientDataAmount>>?

    suspend fun create(foodToCreate: PendingFoodCreate): Pair<PFDao, List<NutrientDataAmount>>

    suspend fun updateStatus(
        pendingFoodReview: PendingFoodReview
    ): Pair<PFDao, List<NutrientDataAmount>>?

    suspend fun countUserSubmissionsOfDay(userId: UUID, date: LocalDate = LocalDate.now(ZoneOffset.UTC)): Int

    suspend fun isDuplicate(food: FoodInformation<NutrientIdWithAmount>): Boolean

    suspend fun delete(pendingFoodId: Int, userId: UUID): Int
}