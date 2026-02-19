package com.example.repository.foods.pending

import com.example.domain.FoodBase
import com.example.domain.PendingFood
import com.example.domain.PendingFoodCreate
import java.time.LocalDate
import java.util.*

interface IPendingFoodsRepository {
    suspend fun create(foodToCreate: PendingFoodCreate): PendingFood
    suspend fun findById(id: Int, userId: UUID): PendingFood?
    suspend fun countUserSubmissionsOfDay(userId: UUID, date: LocalDate = LocalDate.now()): Int
    suspend fun isDuplicateSubmission(userId: UUID, foodBase: FoodBase): Boolean
}