package com.example.repository.foods.pending

import com.example.domain.*
import java.time.LocalDate
import java.util.*

interface IPendingFoodRepository {
    suspend fun findById(id: Int, userId: UUID): PendingFood?

    suspend fun create(foodToCreate: PendingFoodCreate): PendingFood

    /**`
     * Updates the status of a pending food (approve/reject)
     * @return The updated pending food
     */
    suspend fun updateStatus(pendingFoodReview: PendingFoodReview): PendingFood?

    suspend fun countUserSubmissionsOfDay(userId: UUID, date: LocalDate = LocalDate.now()): Int

    /**
     * TODO: Remove and replace with app food repo's `create()`.
     *
     * Moves approved pending food to app_foods table
     * @return The newly created app food `id`
     */
    suspend fun moveToAppFoods(pendingFoodMoveData: PendingFoodMove): Int

    suspend fun isDuplicate(food: FoodInformation<NutrientIdWithAmount>): Boolean

    suspend fun delete(pendingFoodId: Int)
}