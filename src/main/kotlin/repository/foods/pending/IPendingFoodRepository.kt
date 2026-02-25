package com.example.repository.foods.pending

import com.example.domain.*
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.*

interface IPendingFoodRepository {
    suspend fun findByUserType(
        paginationCriteria: PaginationCriteria<PendingFoodsPaginationCriteria>
    ): PaginationQuery<PendingFood>

    suspend fun findByUserId(userId: UUID): List<PendingFood>

    suspend fun findById(id: Int, userId: UUID): PendingFood?

    suspend fun create(foodToCreate: PendingFoodCreate): PendingFood

    suspend fun updateStatus(pendingFoodReview: PendingFoodReview): PendingFood?

    suspend fun countUserSubmissionsOfDay(userId: UUID, date: LocalDate = LocalDate.now(ZoneOffset.UTC)): Int

    suspend fun isDuplicate(food: FoodInformation<NutrientIdWithAmount>): Boolean

    suspend fun delete(pendingFoodId: Int)
}