package com.example.service

import com.example.domain.PendingFood
import com.example.domain.PendingFoodCreate
import com.example.exception.DailySubmissionLimitExceededException
import com.example.exception.DuplicateFoodSubmissionException
import com.example.repository.foods.pending.IPendingFoodsRepository

class PendingFoodService(private val pendingFoodsRepository: IPendingFoodsRepository) {
    companion object {
        const val MAX_DAILY_SUBMISSIONS = 5
    }

    suspend fun add(foodToCreate: PendingFoodCreate): PendingFood {
        // Check daily submission limit
        val submissionCount = pendingFoodsRepository.countUserSubmissionsOfDay(foodToCreate.submittedBy)
        if (submissionCount >= MAX_DAILY_SUBMISSIONS) {
            throw DailySubmissionLimitExceededException()
        }

        // Check for duplicate submission
        val isDuplicate = pendingFoodsRepository.isDuplicateSubmission(
            foodToCreate.submittedBy,
            foodToCreate.information.base
        )
        if (isDuplicate) throw DuplicateFoodSubmissionException()

        return pendingFoodsRepository.create(foodToCreate)
    }
}