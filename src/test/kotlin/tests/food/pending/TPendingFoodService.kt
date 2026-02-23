package tests.food.pending

import com.example.domain.PendingFood
import com.example.repository.foods.app.AppFoodRepository
import com.example.repository.foods.pending.PendingFoodRepository
import com.example.repository.refresh.RefreshRepository
import com.example.repository.user.UserRepository
import com.example.repository.user.wallets.UserWalletRepository
import com.example.service.AuthService
import com.example.service.JwtService
import com.example.service.PendingFoodService
import config.TestDatabase
import mock.auth.createJwtService
import mock.food.buildPendingFoodCreateData
import org.jetbrains.exposed.sql.Database
import org.junit.After
import org.junit.AfterClass
import org.junit.Before
import java.util.*

abstract class TPendingFoodService {
    private lateinit var db: Database

    protected lateinit var refreshRepository: RefreshRepository
    protected lateinit var userRepository: UserRepository
    protected lateinit var userWalletRepository: UserWalletRepository
    protected lateinit var pendingFoodRepository: PendingFoodRepository
    protected lateinit var appFoodRepository: AppFoodRepository
    protected lateinit var jwtService: JwtService
    protected lateinit var authService: AuthService
    protected lateinit var pendingFoodService: PendingFoodService

    companion object {
        @JvmStatic
        @AfterClass
        fun shutDownContainer() {
            TestDatabase.shutDown()
        }
    }

    @Before
    fun setUp() {
        db = TestDatabase.setUp()
        refreshRepository = RefreshRepository()
        userRepository = UserRepository()
        userWalletRepository = UserWalletRepository()
        pendingFoodRepository = PendingFoodRepository()
        appFoodRepository = AppFoodRepository()
        jwtService = createJwtService()
        authService = AuthService(userRepository, refreshRepository, jwtService, userWalletRepository)
        pendingFoodService = PendingFoodService(
            pendingFoodRepository,
            userWalletRepository,
            userRepository,
            appFoodRepository
        )
    }

    @After
    fun tearDown() {
        TestDatabase.tearDown()
    }

    protected suspend fun submitPendingFood(
        submittedBy: UUID = UUID.randomUUID(),
        name: String = "food number ${(1000..9999).random()}"
    ): PendingFood = buildPendingFoodCreateData(submittedBy, name)
        .let { pendingFoodService.add(it) }
}