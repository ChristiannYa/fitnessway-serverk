package tests.auth

import com.auth0.jwt.interfaces.DecodedJWT
import com.example.domain.TokenStrings
import com.example.domain.User
import com.example.domain.UserRegisterData
import com.example.repository.refresh.RefreshRepository
import com.example.repository.user.UserRepository
import com.example.repository.user.wallets.UserWalletRepository
import com.example.service.AuthService
import com.example.service.JwtService
import config.TestDatabase
import io.mockk.every
import io.mockk.mockk
import mock.auth.createJwtService
import org.jetbrains.exposed.sql.Database
import org.junit.After
import org.junit.AfterClass
import org.junit.Before
import java.util.*

abstract class TAuthService {
    private lateinit var db: Database

    protected lateinit var userRepository: UserRepository
    protected lateinit var refreshRepository: RefreshRepository
    protected lateinit var jwtService: JwtService
    protected lateinit var authService: AuthService
    protected lateinit var userWalletRepository: UserWalletRepository

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
        userRepository = UserRepository()
        refreshRepository = RefreshRepository()
        jwtService = createJwtService()
        authService = AuthService(userRepository, refreshRepository, jwtService, userWalletRepository)
    }

    @After
    fun tearDown() {
        TestDatabase.tearDown()
    }

    suspend fun registerAndGetUserData(
        userRegisterData: UserRegisterData = UserRegisterData(
            name = "Test User",
            email = "test@example.com",
            password = "Password123!!"
        ),
        deviceName: String = "HP Envy x360 TEST"
    ): Pair<User, TokenStrings> {
        val userTokens = authService.register(userRegisterData, deviceName)
        val user = userRepository.findByEmail(userRegisterData.email)!!

        return user to userTokens
    }

    fun mockJwtRefreshOperations(userId: UUID, refreshTokenString: String) {
        mockk<DecodedJWT>().let {
            every { it.getClaim("userId") } returns mockk {
                every { asString() } returns userId.toString()
            }

            every { jwtService.verifyToken(refreshTokenString, any()) } returns it
            every { jwtService.extractUserId(it, any()) } returns userId
        }
    }
}