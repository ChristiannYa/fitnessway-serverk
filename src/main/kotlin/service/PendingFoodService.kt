package com.example.service

import com.example.config.RewardConfig
import com.example.domain.*
import com.example.domain.UserType.*
import com.example.exception.*
import com.example.mappers.toCreate
import com.example.repository.foods.app.IAppFoodRepository
import com.example.repository.foods.pending.IPendingFoodRepository
import com.example.repository.user.IUserRepository
import com.example.repository.user.wallets.IUserWalletRepository
import com.example.utils.suspendTransaction
import java.util.*

class PendingFoodService(
    private val pendingFoodRepository: IPendingFoodRepository,
    private val userWalletRepository: IUserWalletRepository,
    private val userRepository: IUserRepository,
    private val appFoodRepository: IAppFoodRepository
) {
    companion object {
        fun UserType.getDailyLimit() = when (this) {
            USER -> 5
            CONTRIBUTOR -> 15
            ADMIN -> 20
        }
    }

    suspend fun findPaginated(
        paginationCriteria: PaginationCriteria<PendingFoodsPaginationCriteria>
    ): PaginationResult<PendingFood> {
        val paginationQuery = pendingFoodRepository.findPaginated(paginationCriteria)

        return PaginationResult(
            data = paginationQuery.data,
            totalCount = paginationQuery.totalCount,
            pageCount = paginationCriteria.calcPageCount(paginationQuery.totalCount.toDouble()),
            currentPage = paginationCriteria.calcCurrentPage()
        )
    }

    suspend fun add(foodToCreate: PendingFoodCreate): PendingFood = suspendTransaction {
        foodToCreate.let {
            val dailyLimit = foodToCreate.userPrincipal.type.getDailyLimit()

            // Check daily submission limit
            val submissionCount = this@PendingFoodService.countUserSubmissionsOfDay(it.userPrincipal.id)
            if (submissionCount >= dailyLimit) throw DailySubmissionLimitExceededException(dailyLimit)

            // Check if food is already in app
            val isAlreadyInApp = appFoodRepository.isDuplicate(it.foodInformation)
            if (isAlreadyInApp) throw FoodAlreadyInAppException()

            // Check if food is already pending
            val isAlreadyPending = pendingFoodRepository.isDuplicate(it.foodInformation)
            if (isAlreadyPending) throw FoodAlreadyPendingException()

            pendingFoodRepository.create(it)
        }
    }

    suspend fun review(pendingFoodReview: PendingFoodReview): PendingFood = suspendTransaction {
        pendingFoodReview.let {
            // Get the pending food to check if it exists
            val pendingFood = pendingFoodRepository.findById(it.pendingFoodId, it.reviewerPrincipal.id)
                ?: throw FoodNotFoundException(
                    "pending food with id ${it.pendingFoodId} not found during revision"
                )

            // Check if already reviewed
            if (pendingFood.status.isReviewed) {
                throw PendingFoodAlreadyReviewedException(
                    "pending food with id ${pendingFood.id} has already been reviewed"
                )
            }

            // Update status
            val reviewedFood = pendingFoodRepository.updateStatus(it)
                ?: throw FoodNotFoundException(
                    "pending food with id ${pendingFood.id} not found when updating review status"
                )

            if (it.isApproved) {
                // Gather information needed to create app food
                val appFoodCreateData = pendingFood.toCreate()
                    ?: throw UserNotFoundException(
                        "pending food author not found when mapping data to create app food"
                    )

                // Create app food based off of request
                appFoodRepository.create(appFoodCreateData)

                // Obtain author type for potential multiplier
                val pendingFoodAuthor = userRepository.findById(appFoodCreateData.createdBy)
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

    suspend fun countUserSubmissionsOfDay(userId: UUID) =
        pendingFoodRepository.countUserSubmissionsOfDay(userId)

    suspend fun dismissReview(pendingFoodId: Int?, userId: UUID) = suspendTransaction {
        // Check pending food `id` is not null or <= 0
        if (pendingFoodId == null || pendingFoodId <= 0) {
            throw InvalidIdException("pending food")
        }

        // Obtain pending food data
        val pendingFood = pendingFoodRepository.findById(pendingFoodId, userId)
            ?: throw FoodNotFoundException("pending food [$pendingFoodId] not found when dismissing review")

        // Throw error if the review still has a PENDING status
        if (pendingFood.status == PendingFoodStatus.PENDING) {
            throw CannotDismissPendingFoodException()
        }

        // Delete review
        pendingFoodRepository.delete(pendingFoodId)
    }
}