package tests

import com.example.repository.edible.app.AppFoodRepository
import com.example.repository.edible.pending.PendingFoodRepository
import com.example.repository.refresh.RefreshRepository
import com.example.repository.user.UserRepository
import com.example.repository.user.wallets.UserWalletRepository
import com.example.service.AppFoodService
import com.example.service.AuthService
import com.example.service.JwtService
import com.example.service.PendingFoodService
import com.example.utils.date_time.DateTimeParser
import com.example.utils.date_time.TimeConverter
import config.TestDatabase
import mock.auth.createJwtService
import org.jetbrains.exposed.sql.Database
import org.junit.After
import org.junit.AfterClass
import org.junit.Before

open class TAppTest {
    private lateinit var db: Database

    protected lateinit var dateTimeParser: DateTimeParser
    protected lateinit var timeConverter: TimeConverter
    protected lateinit var refreshRepository: RefreshRepository
    protected lateinit var userRepository: UserRepository
    protected lateinit var userWalletRepository: UserWalletRepository
    protected lateinit var pendingFoodRepository: PendingFoodRepository
    protected lateinit var appFoodRepository: AppFoodRepository
    protected lateinit var jwtService: JwtService
    protected lateinit var authService: AuthService
    protected lateinit var appEdibleService: AppFoodService
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

        dateTimeParser = DateTimeParser()
        timeConverter = TimeConverter(dateTimeParser)

        userRepository = UserRepository()
        userWalletRepository = UserWalletRepository()

        jwtService = createJwtService()
        refreshRepository = RefreshRepository()
        authService = AuthService(userRepository, refreshRepository, jwtService, userWalletRepository)

        appFoodRepository = AppFoodRepository()
        appEdibleService = AppFoodService(appFoodRepository, timeConverter)

        pendingFoodRepository = PendingFoodRepository()
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
}