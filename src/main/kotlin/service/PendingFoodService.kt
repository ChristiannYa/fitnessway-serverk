package com.example.service

import com.example.config.RewardConfig
import com.example.domain.*
import com.example.exception.*
import com.example.repository.foods.pending.IPendingFoodRepository
import com.example.repository.user.IUserRepository
import com.example.repository.user.wallets.IUserWalletRepository
import com.example.utils.suspendTransaction

class PendingFoodService(
    private val pendingFoodRepository: IPendingFoodRepository,
    private val userWalletRepository: IUserWalletRepository,
    private val userRepository: IUserRepository
) {
    companion object {
        const val MAX_DAILY_SUBMISSIONS = 5
    }

    suspend fun add(foodToCreate: PendingFoodCreate): PendingFood = foodToCreate.let {
        // Check daily submission limit
        val submissionCount = pendingFoodRepository.countUserSubmissionsOfDay(it.submittedBy)
        if (submissionCount >= MAX_DAILY_SUBMISSIONS) {
            throw DailySubmissionLimitExceededException()
        }

        // Check for duplicate submission
        val isDuplicate = pendingFoodRepository.isDuplicateSubmission(it.submittedBy, it.foodInformation.base)
        if (isDuplicate) throw DuplicateFoodSubmissionException()

        pendingFoodRepository.create(foodToCreate)
    }

    suspend fun review(pendingFoodReview: PendingFoodReview): PendingFood = pendingFoodReview.let {
        // Get the pending food to check if it exists
        val pendingFood = pendingFoodRepository.findById(it.pendingFoodId, it.reviewerId)
            ?: throw PendingFoodNotFoundException(
                "pending food with id ${it.pendingFoodId} not found during revision"
            )

        // Check if already reviewed
        if (pendingFood.status.isReviewed()) {
            throw PendingFoodAlreadyReviewedException(
                "pending food with id ${pendingFood.id} has already been reviewed"
            )
        }

        suspendTransaction {
            // Update status
            val reviewedFood = pendingFoodRepository.updateStatus(it)
                ?: throw PendingFoodNotFoundException(
                    "pending food with id ${pendingFood.id} not found when updating review status"
                )

            if (it.isApproved()) {
                // Move pending food to app's foods
                pendingFoodRepository.moveToAppFoods(pendingFood.id)

                // Obtain user type for potential multiplier
                val pendingFoodAuthor = userRepository.findById(reviewedFood.createdBy)
                    ?: throw UserNotFoundException("pending food author not found when reviewing food")

                // Apply multiplier if user is a contributor
                val userCurrency = if (pendingFoodAuthor.type == UserType.CONTRIBUTOR) {
                    RewardConfig.FOOD_APPROVAL_REWARD * RewardConfig.CONTRIBUTOR_MULTIPLIER
                } else RewardConfig.FOOD_APPROVAL_REWARD

                // Reward user for food approval
                userWalletRepository.addCurrency(
                    UserAddCurrency(
                        userId = reviewedFood.createdBy,
                        amount = userCurrency,
                        transactionType = UserTransactionType.FOOD_APPROVAL
                    )
                )
            }

            reviewedFood
        }
    }
}