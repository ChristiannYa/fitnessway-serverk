package tests.food.pending

import com.example.config.RewardConfig
import com.example.domain.*
import com.example.exception.NonAdministratorCannotReviewException
import com.example.exception.PendingFoodAlreadyReviewedException
import com.example.exception.PendingFoodNotFoundException
import com.example.exception.UserNotFoundException
import com.example.mapping.AppFoodDao
import com.example.mapping.PendingFoodDao
import com.example.mapping.UserCurrencyTransactionDao
import com.example.mapping.UserDao
import com.example.repository.AFN
import com.example.repository.U
import com.example.repository.UCT
import com.example.repository.UW
import com.example.utils.suspendTransaction
import kotlinx.coroutines.test.runTest
import mock.user.buildUser
import mock.user.buildUserRegisterData
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.selectAll
import org.junit.Test
import utils.createUserAndGetData
import utils.notNullMessage
import utils.nullMessage
import kotlin.test.*


class ITReviewPendingFood : TPendingFoodService() {
    private suspend fun createAuthorAndReviewer(
        authorType: UserType = UserType.USER,
        reviewerType: UserType = UserType.ADMIN
    ): Pair<User, User> {
        val authorRegisterData = buildUserRegisterData(userType = authorType)
        val reviewerRegisterData = buildUserRegisterData(userType = reviewerType)

        val (author, _) = createUserAndGetData(authService, userRepository, authorRegisterData)
        val (reviewer, _) = createUserAndGetData(authService, userRepository, reviewerRegisterData)

        return author to reviewer
    }

    /**
     * - Creates an author and a reviewer
     * - Builds a pending food review object
     * - Creates a pending food
     */
    private suspend fun arrangeTest(data: PendingFoodArrangeIn): PendingFoodArrangeOut = data.let {
        val (author, reviewer) = createAuthorAndReviewer(it.author.type, it.reviewer.type)
        val createdPendingFood = submitPendingFood(author.id)

        val review = PendingFoodReview(
            pendingFoodId = createdPendingFood.id,
            reviewerPrincipal = reviewer.toPrincipal(),
            rejectionReason = it.rejectionReason
        )

        PendingFoodArrangeOut(
            author = author,
            reviewer = reviewer,
            createdPendingFood = createdPendingFood,
            review = review
        )
    }

    // -------------
    // SUCCESS CASES
    // -------------

    @Test
    fun `approving a pending food as USER type should reward base currency`() = runTest {
        // Arrange
        val arrange = arrangeTest(
            PendingFoodArrangeIn(
                reviewer = buildUser(userType = UserType.ADMIN)
            )
        )

        // Act - review pending food
        val pendingFoodReviewed = pendingFoodService.review(arrange.review)

        // Assert - pending food review is present
        assertNotNull(pendingFoodReviewed, notNullMessage("pendingFoodReviewed"))

        // Assert - pending food status is set to APPROVED by reviewer
        assertEquals(PendingFoodStatus.APPROVED, pendingFoodReviewed.status)

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

            // Assert - transaction type is FOOD APPROVAL
            assertEquals(UserTransactionType.FOOD_APPROVAL, userTransactionDao.transactionType)

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
        val arrange = arrangeTest(
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
        assertEquals(pendingFoodReviewed.status, PendingFoodStatus.APPROVED)

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
        val arrange = arrangeTest(
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
        assertEquals(pendingFoodReviewed.status, PendingFoodStatus.REJECTED)

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


    // ----------
    // FAIL CASES
    // ----------

    private suspend fun assertUntouchedDataBeforeServiceTransaction(arrange: PendingFoodArrangeOut) =
        suspendTransaction {
            // Assert - pending food status is present
            val pendingFoodDao = PendingFoodDao.findById(arrange.createdPendingFood.id)
            assertNotNull(pendingFoodDao, notNullMessage("pendingFoodDao"))

            // Assert - pending food status remains PENDING
            assertEquals(pendingFoodDao.status, PendingFoodStatus.PENDING)

            // Assert - pending food status reviewer is not set
            assertNull(pendingFoodDao.reviewedBy, nullMessage("pendingFoodDao.reviewedBy"))

            // Assert - pending food reviewedAt is not set
            assertNull(pendingFoodDao.reviewedAt, nullMessage("pendingFoodDao.reviewedAt"))

            // Assert - food was NOT moved to the database
            val appFoodDao = AppFoodDao.findById(arrange.createdPendingFood.id)
            assertNull(appFoodDao, nullMessage("appFoodDao"))

            // Assert - user transaction was NOT created
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

    @Test
    fun `reviewing a pending food as non-ADMIN should throw NonAdministratorCannotReviewException`() = runTest {
        // Arrange
        val arrange = arrangeTest(PendingFoodArrangeIn())

        // Act & Assert
        assertFailsWith<NonAdministratorCannotReviewException> {
            pendingFoodService.review(arrange.review)
        }

        assertUntouchedDataBeforeServiceTransaction(arrange)
    }

    @Test
    fun `pending food not found during revision should throws PendingFoodNotFoundException`() = runTest {
        // Arrange
        val (author, reviewer) = createAuthorAndReviewer()
        val createdPendingFood = submitPendingFood(author.id)

        val review = PendingFoodReview(
            pendingFoodId = createdPendingFood.id + (1..10).random(),
            reviewerPrincipal = reviewer.toPrincipal(),
            rejectionReason = null
        )

        // Act & Assert
        assertFailsWith<PendingFoodNotFoundException> {
            pendingFoodService.review(review)
        }
    }

    @Test
    fun `reviewing an already reviewed food throws PendingFoodAlreadyReviewedException`() = runTest {
        // Arrange
        val arrange = arrangeTest(
            PendingFoodArrangeIn(
                reviewer = buildUser(userType = UserType.ADMIN)
            )
        )

        // Act - review pending food
        pendingFoodService.review(arrange.review)

        suspendTransaction {
            // Assert - wallet is present
            val wallet = UW
                .selectAll()
                .where { UW.userId eq arrange.author.id }
                .firstOrNull()
            assertNotNull(wallet, notNullMessage("wallet"))

            // Assert - wallet was updated
            assertEquals(RewardConfig.FOOD_APPROVAL_REWARD.toBigDecimal().setScale(2), wallet[UW.amount])

            // Act && Assert - reviewing the same pending food throws `PendingFoodAlreadyReviewedException`
            assertFailsWith<PendingFoodAlreadyReviewedException> {
                pendingFoodService.review(arrange.review)
            }

            // Assert - wallet was not updated
            assertEquals(RewardConfig.FOOD_APPROVAL_REWARD.toBigDecimal().setScale(2), wallet[UW.amount])
        }
    }

    @Test
    fun `transaction rollback when moveToAppFoods fails should leave database untouched`() = runTest {
        // Arrange
        val arrange = arrangeTest(
            PendingFoodArrangeIn(
                reviewer = buildUser(userType = UserType.ADMIN)
            )
        )

        suspendTransaction {
            // Preemptively insert app food data, so that when we call `review()`, it fails
            // because of a duplicate constraint
            arrange.createdPendingFood.information.base.let {
                AppFoodDao.new {
                    name = it.name
                    brand = it.brand.toString()
                    amountPerServing = it.amountPerServing.toBigDecimal()
                    servingUnit = it.servingUnit
                    createdBy = EntityID(arrange.author.id, U)
                }
            }
        }

        // Act & Assert - constraint violation propagates out of the transaction
        assertFailsWith<Exception> {
            pendingFoodService.review(arrange.review)
        }

        suspendTransaction {
            // Assert - pending food DAO is found
            val pendingFoodDao = PendingFoodDao.findById(arrange.createdPendingFood.id)
            assertNotNull(pendingFoodDao, notNullMessage("pendingFoodDao"))

            // Assert - pending food status remains PENDING (transaction was rolled back)
            assertEquals(PendingFoodStatus.PENDING, pendingFoodDao.status)

            // Assert - pendingFoodDao's `reviewedBy` and `reviewedAt` are not set
            assertNull(pendingFoodDao.reviewedBy, nullMessage("pendingFoodDao.reviewedBy"))
            assertNull(pendingFoodDao.reviewedAt, nullMessage("pendingFoodDao.reviewedAt"))

            // Assert - no user transaction was created
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

            // Assert - wallet is present
            assertEquals(0.toBigDecimal().setScale(2), wallet[UW.amount])
        }
    }

    @Test
    fun `transaction rollback when pending food author not found when mapping move data`() = runTest {
        // Arrange
        val arrange = arrangeTest(
            PendingFoodArrangeIn(
                reviewer = buildUser(userType = UserType.ADMIN)
            )
        )

        suspendTransaction {
            // Delete author - SET NULL cascade sets pending food's created_by to NULL
            // causing toMove() to return null inside the transaction
            UserDao.findById(arrange.author.id)?.delete()
        }

        // Act & Assert
        assertFailsWith<UserNotFoundException> {
            pendingFoodService.review(arrange.review)
        }

        suspendTransaction {
            // Assert - pending food is still present
            val pendingFoodDao = PendingFoodDao.findById(arrange.createdPendingFood.id)
            assertNotNull(pendingFoodDao, notNullMessage("pendingFoodDao"))

            // Assert - pending food status remains PENDING (transaction was rolled back)
            assertEquals(PendingFoodStatus.PENDING, pendingFoodDao.status)

            // Assert - pendingFoodDao's `reviewedBy` and `reviewedAt` are not set
            assertNull(pendingFoodDao.reviewedBy, nullMessage("pendingFoodDao.reviewedBy"))
            assertNull(pendingFoodDao.reviewedAt, nullMessage("pendingFoodDao.reviewedAt"))

            // Assert - food was NOT moved to app_foods (toMove() failed before moveToAppFoods() was called)
            val appFoodDao = AppFoodDao.findById(arrange.createdPendingFood.id)
            assertNull(appFoodDao, nullMessage("appFoodDao"))

            // Assert - no user transaction was created
            val userTransactionDao = UserCurrencyTransactionDao.find {
                UCT.userId eq arrange.author.id
            }.firstOrNull()
            assertNull(userTransactionDao, nullMessage("userTransactionDao"))
        }
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