package tests.food.pending

import com.example.exception.DailySubmissionLimitExceededException
import com.example.exception.FoodAlreadyPendingException
import com.example.mapping.PendingFoodDao
import com.example.utils.suspendTransaction
import kotlinx.coroutines.test.runTest
import mock.food.buildPendingFoodCreateData
import org.junit.Test
import utils.createUserAndGetData
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

class ITAddPendingFood : TPendingFoodService() {
    // -------------
    // SUCCESS CASES
    // -------------

    @Test
    fun `pending food request is saved into the database`() = runTest {
        // Arrange - create user
        val (user, _) = createUserAndGetData(authService, userRepository)
        val pendingFoodRequest = buildPendingFoodCreateData(user.id)

        // Act - create pending food
        val createdPendingFood = pendingFoodService.add(pendingFoodRequest)

        suspendTransaction {
            // Assert - food is found in database
            val pendingFoodDao = PendingFoodDao.findById(createdPendingFood.id)
            assertNotNull(pendingFoodDao, "pendingFoodDao")
        }
    }

    // ----------
    // FAIL CASES
    // ----------

    @Test
    fun `submitting more than 5 requests throws DailySubmissionLimitExceededException`() = runTest {
        // Arrange - create user
        val (user, _) = createUserAndGetData(authService, userRepository)

        // Arrange - submit 5 different requests
        repeat(5) {
            pendingFoodService.add(buildPendingFoodCreateData(user.id))
        }

        // Act & Assert
        assertFailsWith<DailySubmissionLimitExceededException> {
            pendingFoodService.add(buildPendingFoodCreateData(user.id))
        }
    }

    @Test
    fun `submitting an already pending food throws FoodAlreadyPendingException`() = runTest {
        // Arrange - create user
        val (userA, _) = createUserAndGetData(authService, userRepository)
        val (userB, _) = createUserAndGetData(authService, userRepository)
        val (userC, _) = createUserAndGetData(authService, userRepository)

        // Arrange - build users' requests
        val foodName = "Doritos"
        val userARequest = buildPendingFoodCreateData(userA.id, foodName)
        val userBRequest = buildPendingFoodCreateData(userB.id, foodName)
        val userCRequest = buildPendingFoodCreateData(userC.id, foodName)

        // Act - submit user A request
        pendingFoodService.add(userARequest)

        // Act & Assert
        assertFailsWith<FoodAlreadyPendingException> {
            pendingFoodService.add(userBRequest)
        }

        assertFailsWith<FoodAlreadyPendingException> {
            pendingFoodService.add(userCRequest)
        }
    }
}