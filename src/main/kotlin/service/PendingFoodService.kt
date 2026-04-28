package com.example.service

import com.example.config.RewardConfig
import com.example.domain.*
import com.example.domain.UserType.*
import com.example.dto.EdibleAddRequest
import com.example.dto.PendingFoodReviewRequest
import com.example.exception.*
import com.example.mappers.toCategoryGroups
import com.example.mappers.toClientFilter
import com.example.mappers.toCreate
import com.example.mapping.toDto
import com.example.repository.edible.app.IAppFoodRepository
import com.example.repository.edible.pending.IPendingFoodRepository
import com.example.repository.user.IUserRepository
import com.example.repository.user.wallets.IUserWalletRepository
import com.example.utils.suspendTransaction
import com.example.utils.toEnum
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
            ADMIN -> 100
        }
    }

    suspend fun findPaginated(
        paginationCriteria: PaginationCriteria<PendingFoodsPaginationCriteria>
    ): PaginationResult<PendingFood> {
        val paginationQuery = pendingFoodRepository
            .findPaginated(paginationCriteria)
            .getOrElse { throw it }

        return PaginationResult(
            data = paginationQuery.data.map { (pfDao, nutrientList) ->
                pfDao.toDto(
                    nutrientList
                        .toClientFilter(isAppFood = true)
                        .toCategoryGroups()
                )
            },
            totalCount = paginationQuery.totalCount,
            pageCount = paginationCriteria.calcPageCount(paginationQuery.totalCount.toDouble()),
            currentPage = paginationCriteria.calcCurrentPage()
        )
    }

    suspend fun add(
        req: EdibleAddRequest,
        userPrincipal: UserPrincipal
    ): PendingFood = suspendTransaction {
        val dailyLimit = userPrincipal.type.getDailyLimit()

        val submissionCount = this@PendingFoodService.countUserSubmissionsOfDay(userPrincipal.id)
        if (submissionCount >= dailyLimit) throw DailySubmissionLimitExceededException(dailyLimit)

        val isAlreadyInApp = appFoodRepository.isDuplicate(req.base, req.nutrients)
        if (isAlreadyInApp) throw FoodAlreadyInAppException()

        val isAlreadyPending = pendingFoodRepository.isDuplicate(req.base, req.nutrients)
        if (isAlreadyPending) throw FoodAlreadyPendingException()

        val (pfDao, nutrientList) = pendingFoodRepository.create(
            PendingFoodCreate(
                userId = userPrincipal.id,
                base = req.base,
                nutrientList = req.nutrients,
                edibleType = req.edibleType.toEnum()
            )
        )

        pfDao.toDto(
            nutrientList
                .toClientFilter(isAppFood = true)
                .toCategoryGroups()
        )
    }

    suspend fun review(req: PendingFoodReviewRequest, reviewerPrincipal: UserPrincipal): PendingFood =
        suspendTransaction {
            val createdById = UUID.fromString(req.createdById)
                ?: throw InvalidIdException("created by")

            val (pfDao, nutrientList) = pendingFoodRepository.findById(
                req.pendingFoodId,
                createdById,
                reviewerPrincipal.id
            ) ?: throw EdibleNotFoundException(
                "pending food with id ${req.pendingFoodId} not found during revision"
            )

            val nutrients = nutrientList
                .toClientFilter(isAppFood = true)
                .toCategoryGroups()

            val pendingFood = pfDao.toDto(nutrients)

            if (pendingFood.status.isReviewed) {
                throw PendingFoodAlreadyReviewedException(
                    "pending food #${pendingFood.id} has already been reviewed"
                )
            }

            val reviewData = PendingFoodReview(
                createdById = createdById,
                pendingFoodId = req.pendingFoodId,
                reviewerPrincipal = reviewerPrincipal,
                rejectionReason = req.rejectionReason
            )

            val (pfDaoUpdated, nutrientListUpdated) = pendingFoodRepository.updateStatus(reviewData)
                ?: throw EdibleNotFoundException(
                    "pending food #${pendingFood.id} not found when updating review status"
                )

            val reviewedFood = pfDaoUpdated.toDto(
                nutrientListUpdated
                    .toClientFilter(isAppFood = true)
                    .toCategoryGroups()
            )

            if (reviewData.isApproved) {
                val appFoodCreateData = pendingFood.toCreate()
                    ?: throw UserNotFoundException(
                        "pending food author not found when mapping data to create app food"
                    )

                appFoodRepository.create(appFoodCreateData)

                // Obtain author information for potential multiplier
                val pendingFoodAuthor = userRepository.findById(appFoodCreateData.createdBy)
                    ?: throw UserNotFoundException(
                        "pending food author not found when querying" +
                        "for pending food author data"
                    )

                // Apply multiplier if user is a contributor
                val userCurrency = if (pendingFoodAuthor.type == UserType.CONTRIBUTOR) {
                    RewardConfig.FOOD_APPROVAL_REWARD * RewardConfig.CONTRIBUTOR_MULTIPLIER
                } else RewardConfig.FOOD_APPROVAL_REWARD

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

    suspend fun countUserSubmissionsOfDay(userId: UUID) =
        pendingFoodRepository.countUserSubmissionsOfDay(userId)

    suspend fun dismissReview(pendingFoodId: Int, userId: UUID) = suspendTransaction {
        if (pendingFoodId <= 0) {
            throw InvalidIdException("pending food")
        }

        val (pfDao, nutrientList) = pendingFoodRepository.findById(pendingFoodId, userId)
            ?: throw EdibleNotFoundException("pending food #$pendingFoodId not found when dismissing review")

        val nutrients = nutrientList
            .toClientFilter(isAppFood = true)
            .toCategoryGroups()

        val pendingFood = pfDao.toDto(nutrients)

        if (pendingFood.status == PendingFoodStatus.PENDING) {
            throw CannotDismissPendingFoodException()
        }

        pendingFoodRepository.delete(pendingFoodId, userId)
    }
}