package tests.food.pending

import com.example.config.RewardConfig
import com.example.domain.*
import com.example.mapping.AppFoodDao
import com.example.mapping.PendingFoodDao
import com.example.mapping.UserCurrencyTransactionDao
import com.example.repository.AFN
import com.example.repository.UCT
import com.example.repository.UW
import com.example.utils.suspendTransaction
import kotlinx.coroutines.test.runTest
import mock.food.pendingFoodCreateData
import mock.user.buildUser
import mock.user.buildUserRegisterData
import org.jetbrains.exposed.sql.selectAll
import org.junit.Test
import utils.createUserAndGetData
import utils.notNullMessage
import utils.nullMessage
import utils.trueMessage
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ITReviewPendingFood : TPendingFoodService() {
    // -------------
    // SUCCESS CASES
    // -------------

    @Test
    fun `approving a pending food as USER type should reward base currency`() = runTest {
        // Arrange
        val arrange = arrangePendingTest(
            PendingFoodArrangeIn(
                reviewer = buildUser(userType = UserType.ADMIN)
            )
        )

        // Act - review pending food
        val pendingFoodReviewed = pendingFoodService.review(arrange.review)

        // Assert - pending food review is present
        assertNotNull(pendingFoodReviewed, notNullMessage("pendingFoodReviewed"))

        // Assert - pending food status is set to APPROVED by reviewer
        assertTrue(pendingFoodReviewed.status.isApproved())

        suspendTransaction {
            // Assert - pending food is found in database
            val pendingFoodDao = PendingFoodDao.findById(arrange.createdPendingFood.id)
            assertNotNull(pendingFoodDao, notNullMessage("pendingFoodDao"))

            // Assert - pending food status is APPROVED
            assertEquals(PendingFoodStatus.APPROVED, pendingFoodDao.status)

            // Assert - reviewed by is present in database
            val reviewedBy = pendingFoodDao.reviewedBy?.value
            assertNotNull(reviewedBy, notNullMessage("reviewedBy"))

            // Assert - reviewer id is correctly set
            assertEquals(arrange.reviewer.id, reviewedBy)

            // Assert - reviewed at is present
            assertNotNull(pendingFoodDao.reviewedAt, notNullMessage("pendingFoodDao.reviewedAt"))

            // Assert - food moved to database
            val appFoodDao = AppFoodDao.findById(arrange.createdPendingFood.id)
            assertNotNull(appFoodDao, notNullMessage("appFoodDao"))

            // Assert - food created by value is present
            val appFoodAuthor = appFoodDao.createdBy?.value
            assertNotNull(appFoodAuthor, notNullMessage("appFoodAuthor"))

            // Assert - author id is correctly set
            assertEquals(arrange.author.id, appFoodAuthor)

            // Assert - nutrients list is not empty
            val nutrients = AFN
                .selectAll()
                .where { AFN.appFoodId eq appFoodDao.id }
                .toList()
            assertTrue(nutrients.isNotEmpty())

            // Assert - user transaction was created
            val userTransactionDao = UserCurrencyTransactionDao.find {
                UCT.userId eq arrange.author.id
            }.firstOrNull()
            assertNotNull(userTransactionDao, notNullMessage("userTransactionDao"))

            //  Assert - base reward was applied (no multiplier)
            assertEquals(RewardConfig.FOOD_APPROVAL_REWARD.toBigDecimal().setScale(4), userTransactionDao.amount)

            // Assert - wallet is present
            val wallet = UW
                .selectAll()
                .where { UW.userId eq arrange.author.id }
                .firstOrNull()
            assertNotNull(wallet, notNullMessage("wallet"))

            // Assert - wallet was updated
            assertEquals(RewardConfig.FOOD_APPROVAL_REWARD.toBigDecimal().setScale(2), wallet[UW.amount])
        }
    }

    @Test
    fun `approving a pending food as CONTRIBUTOR type should reward currency with multiplier`() = runTest {
        // Arrange
        val arrange = arrangePendingTest(
            PendingFoodArrangeIn(
                author = buildUser(userType = UserType.CONTRIBUTOR),
                reviewer = buildUser(userType = UserType.ADMIN)
            )
        )

        // Act - review pending food
        val pendingFoodReviewed = pendingFoodService.review(arrange.review)

        // Assert - pending food review is present
        assertNotNull(pendingFoodReviewed, notNullMessage("pendingFoodReviewed"))

        // Assert - pending food review status is APPROVED
        assertTrue(pendingFoodReviewed.status.isApproved())

        suspendTransaction {
            // Assert - pending food is found in database
            val pendingFoodDao = PendingFoodDao.findById(arrange.createdPendingFood.id)
            assertNotNull(pendingFoodDao, notNullMessage("pendingFoodDao"))

            // Assert - pending food status is set to APPROVED
            assertEquals(PendingFoodStatus.APPROVED, pendingFoodDao.status)

            // Assert - reviewed by is present in database
            val reviewedBy = pendingFoodDao.reviewedBy?.value
            assertNotNull(reviewedBy, notNullMessage("reviewedBy"))

            // Assert - reviewer id is correctly set
            assertEquals(arrange.reviewer.id, reviewedBy)

            // Assert - reviewed at is present
            assertNotNull(pendingFoodDao.reviewedAt, notNullMessage("pendingFoodDao.reviewedAt"))

            // Assert - food moved to database
            val appFoodDao = AppFoodDao.findById(arrange.createdPendingFood.id)
            assertNotNull(appFoodDao, notNullMessage("appFoodDao"))

            // Assert - food created by value is present
            val appFoodAuthor = appFoodDao.createdBy?.value
            assertNotNull(appFoodAuthor, notNullMessage("appFoodAuthor"))

            // Assert - author id is correctly set
            assertEquals(arrange.author.id, appFoodAuthor)

            // Assert - nutrients list is not empty
            val nutrients = AFN
                .selectAll()
                .where { AFN.appFoodId eq appFoodDao.id }
                .toList()
            assertTrue(nutrients.isNotEmpty())

            // Assert - user transaction was created
            val userTransactionDao = UserCurrencyTransactionDao.find {
                UCT.userId eq arrange.author.id
            }.firstOrNull()
            assertNotNull(userTransactionDao, notNullMessage("userTransactionDao"))

            // Assert - wallet is present
            val wallet = UW
                .selectAll()
                .where { UW.userId eq arrange.author.id }
                .firstOrNull()
            assertNotNull(wallet, notNullMessage("wallet"))

            // Assert - multiplied reward was applied
            val expectedReward = (RewardConfig.FOOD_APPROVAL_REWARD * RewardConfig.CONTRIBUTOR_MULTIPLIER)
                .toBigDecimal().setScale(2)
            assertEquals(expectedReward, wallet[UW.amount])
        }
    }

    @Test
    fun `rejecting a pending food with a valid reason should update status and set rejection metadata`() = runTest {
        // Arrange
        val arrange = arrangePendingTest(
            PendingFoodArrangeIn(
                reviewer = buildUser(userType = UserType.ADMIN),
                rejectionReason = "Nutritional values are inaccurate"
            )
        )

        // Act - review pending food
        val pendingFoodReviewed = pendingFoodService.review(arrange.review)

        // Assert - pending food review is present
        assertNotNull(pendingFoodReviewed, notNullMessage("pendingFoodReviewed"))

        // Assert - pending food review is REJECTED
        assertTrue(pendingFoodReviewed.status.isRejected(), trueMessage("pendingFoodReviewed.status.isRejected()"))

        suspendTransaction {
            // Assert - pending food is found in database
            val pendingFoodDao = PendingFoodDao.findById(arrange.createdPendingFood.id)
            assertNotNull(pendingFoodDao, notNullMessage("pendingFoodDao"))

            // Assert - pending food status is set to REJECTED
            assertEquals(PendingFoodStatus.REJECTED, pendingFoodDao.status)

            // Assert - reviewed by is present in database
            val reviewedBy = pendingFoodDao.reviewedBy?.value
            assertNotNull(reviewedBy, notNullMessage("reviewedBy"))

            // Assert - reviewer id is correctly set
            assertEquals(arrange.reviewer.id, reviewedBy)

            // Assert - reviewed at is present
            assertNotNull(pendingFoodDao.reviewedAt, notNullMessage("pendingFoodDao.reviewedAt"))

            // Assert - food is NOT moved to database
            val appFoodDao = AppFoodDao.findById(arrange.createdPendingFood.id)
            assertNull(appFoodDao, nullMessage("appFoodDao"))

            // Assert - NO user transaction was created
            val userTransactionDao = UserCurrencyTransactionDao.find {
                UCT.userId eq arrange.author.id
            }.firstOrNull()
            assertNull(userTransactionDao, nullMessage("userTransactionDao"))

            // Assert - wallet is present
            val wallet = UW
                .selectAll()
                .where { UW.userId eq arrange.author.id }
                .firstOrNull()
            assertNotNull(wallet, notNullMessage("wallet"))

            // Assert - wallet is still at 0
            assertEquals(0.toBigDecimal().setScale(2), wallet[UW.amount])
        }
    }

    private suspend fun arrangePendingTest(data: PendingFoodArrangeIn): PendingFoodArrangeOut = data.let {
        val authorRegisterData = buildUserRegisterData(userType = it.author.type)
        val reviewerRegisterData = buildUserRegisterData(userType = it.reviewer.type)

        val (author, _) = createUserAndGetData(authService, userRepository, authorRegisterData)
        val (reviewer, _) = createUserAndGetData(authService, userRepository, reviewerRegisterData)

        val createdPendingFood = pendingFoodCreateData.copy(
            submittedBy = author.id
        ).let { f -> pendingFoodService.add(f) }

        val review = PendingFoodReview(
            pendingFoodId = createdPendingFood.id,
            reviewerId = reviewer.id,
            rejectionReason = it.rejectionReason
        )

        PendingFoodArrangeOut(
            author = author,
            reviewer = reviewer,
            createdPendingFood = createdPendingFood,
            review = review
        )
    }
}

private data class PendingFoodArrangeIn(
    val author: User = buildUser(),
    val reviewer: User = buildUser(),
    val rejectionReason: String? = null
)

private data class PendingFoodArrangeOut(
    val author: User,
    val reviewer: User,
    val createdPendingFood: PendingFood,
    val review: PendingFoodReview
)