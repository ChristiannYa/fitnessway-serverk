package com.example.repository.foods.pending

import com.example.domain.FoodBase
import com.example.domain.PendingFood
import com.example.domain.PendingFoodCreate
import com.example.domain.PendingFoodReview
import java.time.LocalDate
import java.util.*

interface IPendingFoodRepository {
    suspend fun findById(id: Int, userId: UUID): PendingFood?

    suspend fun create(foodToCreate: PendingFoodCreate): PendingFood

    /**
     * Updates the status of a pending food (approve/reject)
     * @return The updated pending food
     */
    suspend fun updateStatus(pendingFoodReview: PendingFoodReview): PendingFood?

    suspend fun countUserSubmissionsOfDay(userId: UUID, date: LocalDate = LocalDate.now()): Int

    /**
     * Moves approved pending food to app_foods table
     * @return The newly created app food `id`
     */
    suspend fun moveToAppFoods(pendingFoodId: Int): Int

    suspend fun isDuplicateSubmission(userId: UUID, foodBase: FoodBase): Boolean

    suspend fun delete(pendingFoodId: Int)
}