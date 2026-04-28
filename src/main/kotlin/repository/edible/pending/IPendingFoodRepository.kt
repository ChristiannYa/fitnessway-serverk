package com.example.repository.edible.pending

import com.example.domain.*
import com.example.mapping.PEDao
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.*

interface IPendingFoodRepository {
    suspend fun findPaginated(
        paginationCriteria: PaginationCriteria<PendingFoodsPaginationCriteria>
    ): Result<PaginationQuery<Pair<PEDao, List<NutrientDataAmount>>>>

    suspend fun findById(
        id: Int,
        createdById: UUID,
        reviewerId: UUID? = null
    ): Pair<PEDao, List<NutrientDataAmount>>?

    suspend fun create(foodToCreate: PendingFoodCreate): Pair<PEDao, List<NutrientDataAmount>>

    suspend fun updateStatus(
        pendingFoodReview: PendingFoodReview
    ): Pair<PEDao, List<NutrientDataAmount>>?

    suspend fun countUserSubmissionsOfDay(userId: UUID, date: LocalDate = LocalDate.now(ZoneOffset.UTC)): Int

    suspend fun isDuplicate(
        base: EdibleBase,
        nutrientList: List<NutrientIdWithAmount>
    ): Boolean

    suspend fun delete(pendingFoodId: Int, userId: UUID): Int
}