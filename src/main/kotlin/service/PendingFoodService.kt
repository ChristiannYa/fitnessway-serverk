package com.example.service

import com.example.config.RewardConfig
import com.example.domain.*
import com.example.exception.*
import com.example.repository.foods.app.IAppFoodRepository
import com.example.repository.foods.pending.IPendingFoodRepository
import com.example.repository.user.IUserRepository
import com.example.repository.user.wallets.IUserWalletRepository
import com.example.utils.suspendTransaction

class PendingFoodService(
    private val pendingFoodRepository: IPendingFoodRepository,
    private val userWalletRepository: IUserWalletRepository,
    private val userRepository: IUserRepository,
    private val appFoodRepository: IAppFoodRepository
) {
    companion object {
        const val MAX_DAILY_SUBMISSIONS = 5
    }

    suspend fun add(foodToCreate: PendingFoodCreate): PendingFood = foodToCreate.let {
        // Check daily submission limit
        val submissionCount = pendingFoodRepository.countUserSubmissionsOfDay(it.author)
        if (submissionCount >= MAX_DAILY_SUBMISSIONS) throw DailySubmissionLimitExceededException()

        // Check if food is already in app
        val isAlreadyInApp = appFoodRepository.isDuplicate(it.foodInformation)
        if (isAlreadyInApp) throw FoodAlreadyInAppException()

        // Check if food is already pending
        val isAlreadyPending = pendingFoodRepository.isDuplicate(it.foodInformation)
        if (isAlreadyPending) throw FoodAlreadyPendingException()

        pendingFoodRepository.create(foodToCreate)
    }

    suspend fun review(pendingFoodReview: PendingFoodReview): PendingFood = pendingFoodReview.let {
        // Check if reviewer is an administrator
        if (!pendingFoodReview.canReview) throw NonAdministratorCannotReviewException()

        // Get the pending food to check if it exists
        val pendingFood = pendingFoodRepository.findById(it.pendingFoodId, it.reviewerPrincipal.id)
            ?: throw PendingFoodNotFoundException(
                "pending food with id ${it.pendingFoodId} not found during revision"
            )

        // Check if already reviewed
        if (pendingFood.status.isReviewed) {
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

            if (it.isApproved) {
                // Gather information needed to move pending food to `app_foods` table
                val pendingFoodMoveData = pendingFood.toMove()
                    ?: throw UserNotFoundException(
                        "pending food author not found when mapping move data" +
                        " from pending food"
                    )

                // Move pending food to app's foods
                pendingFoodRepository.moveToAppFoods(pendingFoodMoveData)

                // Obtain author type for potential multiplier
                val pendingFoodAuthor = userRepository.findById(pendingFoodMoveData.authorId)
                    ?: throw UserNotFoundException(
                        "pending food author not found when querying" +
                        "for pending food author data"
                    )

                // Apply multiplier if user is a contributor
                val userCurrency = if (pendingFoodAuthor.type == UserType.CONTRIBUTOR) {
                    RewardConfig.FOOD_APPROVAL_REWARD * RewardConfig.CONTRIBUTOR_MULTIPLIER
                } else RewardConfig.FOOD_APPROVAL_REWARD

                // Reward user for food approval
                userWalletRepository.addCurrency(
                    UserAddCurrency(
                        userId = pendingFoodAuthor.id,
                        amount = userCurrency,
                        transactionType = UserTransactionType.FOOD_APPROVAL
                    )
                )
            }

            reviewedFood
        }
    }
}