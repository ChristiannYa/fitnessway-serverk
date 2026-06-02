package tests.edible.pending

import com.example.domain.PendingFoodReview
import com.example.domain.UserType
import com.example.exception.DailySubmissionLimitExceededException
import com.example.exception.FoodAlreadyInAppException
import com.example.exception.FoodAlreadyPendingException
import com.example.mappers.toAddRequest
import com.example.mappers.toPrincipal
import com.example.mappers.toRequest
import com.example.mapping.PEDao
import com.example.utils.suspendTransaction
import kotlinx.coroutines.test.runTest
import mock.edible.buildPendingFoodCreateData
import mock.user.buildUserRegisterData
import org.junit.Test
import utils.createAndGetUserData
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

class ITAddPendingFood : TPendingFoodService() {
    // -------------
    // SUCCESS CASES
    // -------------

    @Test
    fun `pending food request is saved into the database`() = runTest {
        // Arrange - create user
        val (user, _) = createAndGetUserData(authService, userRepository)
        val pendingFoodRequest = buildPendingFoodCreateData(user.id)

        // Act - create pending food
        val createdPendingFood = pendingFoodService.add(
            req = pendingFoodRequest.toAddRequest(),
            user.toPrincipal()
        )

        suspendTransaction {
            // Assert - food is found in database
            val peDao = PEDao.findById(createdPendingFood.id)
            assertNotNull(peDao, "pendingFoodDao")
        }
    }

    // ----------
    // FAIL CASES
    // ----------

    @Test
    fun `submitting more than 5 requests throws DailySubmissionLimitExceededException`() = runTest {
        // Arrange - create user
        val (user, _) = createAndGetUserData(authService, userRepository)

        // Arrange - submit 5 different requests
        repeat(5) {
            pendingFoodService.add(
                req = buildPendingFoodCreateData(user.id).toAddRequest(),
                user.toPrincipal()
            )
        }

        // Act & Assert
        assertFailsWith<DailySubmissionLimitExceededException> {
            pendingFoodService.add(
                req = buildPendingFoodCreateData(user.id).toAddRequest(),
                user.toPrincipal()
            )
        }
    }

    @Test
    fun `submitting an already pending food throws FoodAlreadyPendingException`() = runTest {
        // Arrange - create user
        val (userA, _) = createAndGetUserData(authService, userRepository)
        val (userB, _) = createAndGetUserData(authService, userRepository)
        val (userC, _) = createAndGetUserData(authService, userRepository)

        // Arrange - build users' requests
        val foodName = "Doritos"
        val userARequest = buildPendingFoodCreateData(userA.id, foodName)
        val userBRequest = buildPendingFoodCreateData(userB.id, foodName)
        val userCRequest = buildPendingFoodCreateData(userC.id, foodName)

        // Act - submit user A request
        pendingFoodService.add(
            req = userARequest.toAddRequest(),
            userPrincipal = userA.toPrincipal()
        )

        // Act & Assert
        assertFailsWith<FoodAlreadyPendingException> {
            pendingFoodService.add(
                req = userBRequest.toAddRequest(),
                userPrincipal = userB.toPrincipal()
            )
        }

        assertFailsWith<FoodAlreadyPendingException> {
            pendingFoodService.add(
                req = userCRequest.toAddRequest(),
                userPrincipal = userC.toPrincipal()
            )
        }
    }

    @Test
    fun `submitting an already app food throws FoodAlreadyInAppException`() = runTest {
        // Arrange - create user and reviewer
        val (user, _) = createAndGetUserData(authService, userRepository)
        val (reviewer, _) = createAndGetUserData(
            authService,
            userRepository,
            buildUserRegisterData(userType = UserType.ADMIN)
        )

        // Arrange - build user food register requests
        val userFoodRequestA = buildPendingFoodCreateData(user.id, "Doritos")
        val userFoodRequestB = buildPendingFoodCreateData(user.id, "Doritos")

        // Arrange - submit user app food request A
        val createdPendingFoodA = pendingFoodService.add(
            req = userFoodRequestA.toAddRequest(),
            userPrincipal = user.toPrincipal()
        )

        // Arrange - build review object
        val reviewA = PendingFoodReview(
            createdById = user.id,
            pendingFoodId = createdPendingFoodA.id,
            reviewerPrincipal = reviewer.toPrincipal(),
            rejectionReason = null
        )

        // Arrange - review pending food A
        pendingFoodService.review(
            req = reviewA.toRequest(),
            reviewerPrincipal = reviewA.reviewerPrincipal
        )

        // Act & Assert
        assertFailsWith<FoodAlreadyInAppException> {
            pendingFoodService.add(
                req = userFoodRequestB.toAddRequest(),
                userPrincipal = user.toPrincipal()
            )
        }
    }
}