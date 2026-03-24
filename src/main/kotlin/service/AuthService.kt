package com.example.service

import com.example.config.TokenDuration
import com.example.domain.*
import com.example.exception.InvalidCredentialsException
import com.example.exception.UserAlreadyExistsException
import com.example.exception.UserNotFoundException
import com.example.mappers.toPrincipal
import com.example.repository.refresh.IRefreshRepository
import com.example.repository.user.IUserRepository
import com.example.repository.user.wallets.UserWalletRepository
import com.example.utils.hashPassword
import com.example.utils.hashToken
import com.example.utils.suspendTransaction
import com.example.utils.verifyPassword
import mu.KotlinLogging
import java.time.Instant
import java.util.*

private val logger = KotlinLogging.logger {}

class AuthService(
    private val userRepository: IUserRepository,
    private val refreshRepository: IRefreshRepository,
    private val jwtService: JwtService,
    private val userWalletRepository: UserWalletRepository
) {
    suspend fun login(userLoginData: UserLoginData, deviceName: String): TokenStrings = suspendTransaction {
        val user = userRepository.findByEmail(userLoginData.email)
            ?: throw InvalidCredentialsException()

        if (!verifyPassword(userLoginData.password, user.passwordHash))
            throw InvalidCredentialsException()

        handleTokens(user.toPrincipal(), deviceName)
    }

    suspend fun register(userRegisterData: UserRegisterData, deviceName: String): TokenStrings = suspendTransaction {
        if (userRepository.findByEmail(userRegisterData.email) != null)
            throw UserAlreadyExistsException()

        val passwordHash = hashPassword(userRegisterData.password)

        val user = userRepository.create(
            UserCreate(
                userRegisterData.name,
                userRegisterData.email,
                passwordHash,
            )
        )

        userWalletRepository.createWallet(user.id)

        handleTokens(user.toPrincipal(), deviceName)
    }

    suspend fun refreshAccessToken(refreshTokenString: String): String = suspendTransaction {
        // Extract user id from the decoded JWT
        val decodedJwt = jwtService.verifyToken(refreshTokenString, TokenType.REFRESH)
        val userId = jwtService.extractUserId(decodedJwt, TokenType.REFRESH)

        // Obtain refresh token
        val refreshTokenHash = hashToken(refreshTokenString)
        val validationResult = refreshRepository.validate(refreshTokenHash, userId)
        val refreshToken = validationResult.getTokenOrThrow()

        // Update last used time before generating access token, that way if generation
        // fails the timestamps will show that an attempt was made
        refreshRepository.updateLastUsedTime(refreshToken.hash)

        val user = userRepository.findById(userId)
            ?: throw UserNotFoundException("user not found while refreshing access token")

        // Generate new access token
        jwtService.generateAccessToken(
            AccessTokenClaims(
                sessionId = refreshToken.id.toString(),
                userPrincipal = user.toPrincipal()
            )
        )
    }

    private suspend fun handleTokens(userPrincipal: UserPrincipal, deviceName: String): TokenStrings {
        val refreshToken = jwtService.generateRefreshToken(
            RefreshTokenClaims(
                userId = userPrincipal.id.toString()
            )
        )

        val refreshTokenDb = saveRefreshTokenInDb(
            userId = userPrincipal.id,
            refreshToken = refreshToken,
            deviceName = deviceName
        )

        val accessToken = jwtService.generateAccessToken(
            AccessTokenClaims(
                sessionId = refreshTokenDb.id.toString(),
                userPrincipal = userPrincipal
            )
        )

        return TokenStrings(accessToken, refreshToken)
    }

    private suspend fun saveRefreshTokenInDb(
        userId: UUID,
        refreshToken: String,
        deviceName: String
    ): RefreshToken {
        val refreshTokenHash = hashToken(refreshToken)
        val expiresAt = Instant.now().plus(TokenDuration.REFRESH_TOKEN)

        return refreshRepository.save(
            RefreshTokenCreate(
                hash = refreshTokenHash,
                userId = userId,
                expiresAt = expiresAt,
                deviceName = deviceName
            )
        )
    }

    suspend fun logout(refreshToken: String) {
        val refreshTokenHash = hashToken(refreshToken)
        val isRefreshTokenRevoked = refreshRepository.revokeByHash(refreshTokenHash)

        if (!isRefreshTokenRevoked) {
            logger.warn("Attempted to revoke non-existent refresh token")
        }
    }
}